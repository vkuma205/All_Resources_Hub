import org.apache.spark.{SparkConf, SparkContext}

object workshop08 {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("DailyRevenue").setMaster("local")
    val sc = new SparkContext(conf)
    val orders = sc.textFile("/home/vinodkumargardas/retail_db/orders")
  }

}
