import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.apache.spark.sql.functions.{current_timestamp, lit, date_format}
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions.{avg, max, min, row_number}
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.pubsub.SparkPubsubMessage
import util.Utils.stationsSchema
import util.Utils.getStation

object Metrics {
  val elevationCol = "elevation"

  def minMaxLvl(data: DataFrame): DataFrame =
    data
      .agg(
        min(elevationCol),
        max(elevationCol),
      )

  def avgLvl(data: DataFrame): DataFrame =
    data
      .agg(
        avg(elevationCol)
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
    stream.window(Seconds(windowInterval), Seconds(slidingInterval))
      .foreachRDD {
        rdd =>
          val stationsDF = spark.createDataFrame(getStation(rdd), stationsSchema)
            .withColumn("timestamp", lit(date_format(current_timestamp(), "dd.MM.yyyy_hh-mm")))
            .cache()

          minMaxLvl(stationsDF).write.format("bigquery").option("table", "levels.min__and_max_levels")
            .option("temporaryGcsBucket","prod-eu-data_and_other").mode(SaveMode.Append).save()
          avgLvl(stationsDF).write.format("bigquery").option("table", "levels.average_level")
            .option("temporaryGcsBucket","prod-eu-data_and_other").mode(SaveMode.Append).save()
          medianLvl(stationsDF, spark).write.format("bigquery").option("table", "levels.median_level")
            .option("temporaryGcsBucket","prod-eu-data_and_other").mode(SaveMode.Append).save()

          stationsDF.write.mode(SaveMode.Append).partitionBy("timestamp")
            .parquet("gs://prod-eu-data_and_other/data/")
      }
  }
}
