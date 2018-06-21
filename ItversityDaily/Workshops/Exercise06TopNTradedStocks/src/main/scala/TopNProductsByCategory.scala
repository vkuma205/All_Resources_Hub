import org.apache.spark.{SparkConf, SparkContext}
object TopNProductsByCategory {
  def getTopNPricedProducts(rec: (Int, Iterable[String]), topN: Int): Iterable[String] = {
  //Exract all the prices into a collection
  val productsList = rec._2.toList
  val topNPrices = productsList.
    map(x => x.split(",")(4).toFloat).
    sortBy(x => -x).
    distinct.
    slice(0, topN)

  val topNPricedProducts = productsList.
    sortBy(x => -x.split(",")(4).toFloat).
    filter(x => topNPrices.contains(x.split(",")(4).toFloat))

  topNPricedProducts
}

  def main(args: Array[String]): Unit = {
    val topN = args(0).toInt
    val conf = new SparkConf().setAppName("Top" + topN + "priced products in category - simulating dense rank").setMaster("local")
    val sc = new SparkContext(conf)
    val products = sc.textFile("")
    val productsFiltered = products.
      filter(rec => rec.split(",")(0).toInt != 685)
    val productsMap = productsFiltered.map(rec => (rec.split(",")(1).toInt, rec))
    val productsGBK = productsMap.groupByKey()
    productsGBK.
      flatMap(rec => getTopNPricedProducts(rec, topN)).
      collect().
      foreach(println)
  }
}
