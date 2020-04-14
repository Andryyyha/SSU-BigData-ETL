
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions.{avg, max, min, row_number}
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.pubsub.SparkPubsubMessage
import util.Utils.stationsSchema
import util.Utils.getStation
import util.Utils.writeToTable
import util.Utils.writeToParquet

object Metrics {
  val elevationCol = "elevation"

  def minMaxLvl(data: DataFrame): DataFrame =
    data
      .agg(
        min(elevationCol).alias("min_elevation"),
        max(elevationCol).alias("max_elevation"))

  def avgLvl(data: DataFrame): DataFrame =
    data
      .agg(
        avg(elevationCol).alias("avg_elevation")
      )

  def medianLvl(data: DataFrame, spark: SparkSession): DataFrame = {
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

  def startMetrics(stream: DStream[SparkPubsubMessage], windowInterval: Int, slidingInterval: Int,
                   spark: SparkSession): Unit = {
    stream.window(Seconds(windowInterval), Seconds(slidingInterval)).foreachRDD {
      rdd =>
        val stationsDF = spark.createDataFrame(getStation(rdd), stationsSchema).cache()

        stationsDF.show(500)
        writeToTable(minMaxLvl(stationsDF), "levels.min__and_max_levels")
        writeToTable(avgLvl(stationsDF),"levels.average_level" )
        writeToTable( medianLvl(stationsDF, spark),"levels.median_level" )
        writeToParquet(stationsDF)
    }
  }
}
