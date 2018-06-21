import org.apache.spark.SparkContext._
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.hadoop.fs._
import com.typesafe.config._
//import scala.util.matching._
def main(args : Array[String]) {


  val props = ConfigFactory.load()
  val conf = new SparkConf().
    setAppName("Top N products" ).
    setMaster(props.getConfig(args(0)).getString("deploymentMode"))
  val sc = new SparkContext(conf)


  val fileInput = args(1)
  val fileOutput = args(2)

  val fs = FileSystem.get(sc.hadoopConfiguration)
  val inputPathExists = fs.exists(new Path(fileInput))
  val outputPathExists = fs.exists(new Path(fileOutput))

  if (! inputPathExists) {
    println("Input file not exists")
    return
  }

  if ( outputPathExists) {
    println("deleted output dir ")
    fs.delete(new Path(fileOutput),true)
  }

  val fileReadRdd = sc.textFile(fileInput)
  val stripCurly = "[{~,!,@,#,$,%,^,&,*,(,),_,=,-,`,:,',?,/,<,>,.}]"
  val fileReadRdd2 = fileReadRdd.map(x => stripCurly.replaceAll(x,""))

  val fileFlat = fileReadRdd.flatMap(rec => rec.split(" "))
  val fileOne = fileFlat.map(rec => (rec,1))
  val fileReduce = fileOne.reduceByKey(_+_)
  // fileReduce.take(105).foreach(println)
  fileReduce.saveAsTextFile(fileOutput)

}