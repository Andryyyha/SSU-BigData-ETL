import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}

object Main {

  val elevationCol = "elevation"

  val stationsSchema = StructType(
    List(
      StructField("id", StringType, nullable = true),
      StructField("name", StringType, nullable = true),
      StructField("address", StringType, nullable = true),
      StructField("lon", StringType, nullable = true),
      StructField("lat", StringType, nullable = true),
      StructField("elevation", IntegerType, nullable = true)
    )
  )

  def minMaxLvl(data: DataFrame) =
    data
      .agg(
        min(elevationCol),
        max(elevationCol),
      )

  def avgLvl(data: DataFrame) =
    data
      .agg(
        avg(elevationCol)
      )

  def medianLvl(data: DataFrame, spark: SparkSession) = {
    val w = Window.orderBy("id")
    val dataWithIndex = data.withColumn("index", row_number().over(w))
    val count = data.count().toInt
    val halfCount = count / 2
    val nextHalfCount = halfCount + 1
    var median = 0.0
    if (count % 2 == 0) {
      val left = dataWithIndex.where(s"index == $halfCount").select(elevationCol).collect.map(x => x.get(0)).mkString.toDouble
      val right = dataWithIndex.where(s"index == $nextHalfCount").select(elevationCol).collect.map(x => x.get(0)).mkString.toDouble
      median = (left + right) / 2
    } else {
      median = dataWithIndex.where(s"index == $halfCount").select(elevationCol).collect.map(x => x.get(0)).mkString.toDouble
    }
    import spark.implicits._
    val result = Seq(median).toDF("median")
    result
  }

  def main(args: Array[String]) : Unit = {
    val spark: SparkSession = SparkSession
      .builder()
      .appName("testApp")
      .config("spark.master", "local[2]")
      .getOrCreate()

    val airDF = spark.read
      .format("csv")
      .option("header", "true")
      .option("mode", "DROPMALFORMED")
      .load("test-air.csv")

    val stationsDF = spark.read
      .format("csv")
      .option("header", "true")
      .schema(stationsSchema)
      .load("test-stations.csv")

    minMaxLvl(stationsDF).show()
    avgLvl(stationsDF).show()
    medianLvl(stationsDF, spark).show()
  }
}