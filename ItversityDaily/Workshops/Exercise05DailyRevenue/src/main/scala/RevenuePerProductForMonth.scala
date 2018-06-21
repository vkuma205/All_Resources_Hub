import com.typesafe.config.ConfigFactory
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.{SparkConf, SparkContext}
import scala.io.Source
object RevenuePerProductForMonth {
  def main(args: Array[String]): Unit = {
    val inputPath = args(1)
    val outputPath = args(2)
    val month = args(4)
    val props = ConfigFactory.load()
    val envProps = props.getConfig(args(0))
    val conf = new SparkConf().
      setAppName("Revenue Per Product for " + month).
      setMaster(envProps.getString("executionMode"))
    val sc = new SparkContext(conf)

    val fs = FileSystem.get(sc.hadoopConfiguration)

    if (!fs.exists(new Path(inputPath))) {
      println("Input path does not exist")
    } else {
      if (fs.exists(new Path(outputPath)))
        fs.delete(new Path(outputPath), true)

      // Filter for orders which fall in the month passed as argument
      val orders = inputPath + "/orders"
      val ordersFiltered = sc.textFile(orders).
        filter(order => order.split(",")(1).contains(month)).
        map(order => (order.split(",")(0).toInt, 1))

      // Join filtered orders and order_items to get order_item details for a given month
      // Get revenue for each product_id
      val orderItems = inputPath + "/order_items"
      val revenueByProductId = sc.textFile(orderItems).
        map(orderItem => {
          val oi = orderItem.split(",")
          (oi(1).toInt, (oi(2).toInt, oi(4).toFloat))
        }).
        join(ordersFiltered).
        map(rec => rec._2._1).
        reduceByKey(_ + _)

      // We need to read products from local file system
      val localPath = args(3)
      val products = Source.
        fromFile(localPath  + "/products/part-00000").
        getLines()

      // Convert into RDD and extract product_id and product_name
      // Join it with aggregated order_items (product_id, revenue)
      // Get product_name and revenue for each product
      sc.parallelize(products.toList).
        map(product => (product.split(",")(0).toInt, product.split(",")(2))).
        join(revenueByProductId).
        map(rec => rec._2.productIterator.mkString("\t")).
        saveAsTextFile(outputPath)
    }
  }

}
