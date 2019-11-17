import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession

trait SparkSupport {
  // make spark session and spark context
  implicit lazy val spark: SparkSession = {
    val spark = SparkSession.builder
      .master("local[*]")
      .appName("SparkTestApp")
      .config("spark.driver.host", "localhost")
      .getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")
    spark
  }

  implicit lazy val sparkContext: SparkContext = spark.sparkContext
}
