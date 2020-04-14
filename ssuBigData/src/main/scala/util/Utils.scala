package util

import java.nio.charset.StandardCharsets

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.functions.{current_timestamp, date_format, lit}
import org.apache.spark.sql.{DataFrame, Row, SaveMode}
import org.apache.spark.sql.types.{DoubleType, IntegerType, StringType, StructField, StructType}
import org.apache.spark.streaming.pubsub.SparkPubsubMessage

object Utils {

  val pattern = "yyyy-MM-dd_hh:mm"
  val bucketName = "prod-eu-data_and_other"
  val pathToParquet = "gs://prod-eu-data_and_other/data/"

  val stationsSchema: StructType = StructType(
    List(
      StructField("id", IntegerType, nullable = false),
      StructField("name", StringType, nullable = false),
      StructField("address", StringType, nullable = false),
      StructField("lon", DoubleType, nullable = false),
      StructField("lat", DoubleType, nullable = false),
      StructField("elevation", IntegerType, nullable = false)
    )
  )

  def getStation(input: RDD[SparkPubsubMessage]): RDD[Row] = {
    input.map(message => new String(message.getData(), StandardCharsets.UTF_8))
      .filter(_.length != 0)
      .map(_.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"))
      .map {
        attribute =>
          List(toInt(attribute(0)),
            attribute(1),
            attribute(2),
            toDouble(attribute(3)),
            toDouble(attribute(4)),
            toInt(attribute(5))
          )
      }
      .map(attribute => Row.fromSeq(attribute))
  }

  def writeToTable(df: DataFrame, tableName: String ): Unit = {
    df.withColumn("timestamp", date_format(lit(current_timestamp()), pattern))
      .write.format("bigquery").option("table", tableName)
      .option("temporaryGcsBucket", bucketName).mode(SaveMode.Append).save()
  }

  def writeToParquet(df: DataFrame): Unit = {
    df.withColumn("timestamp", date_format(lit(current_timestamp()), pattern))
      .write.mode(SaveMode.Append).parquet(pathToParquet)
  }

  def toDouble(r: String):Double = {
    try {
      r.toDouble
    } catch {
      case e: Exception => 0.0
    }
  }

  def toInt(r: String): Int = {
    try {
      r.toInt
    } catch {
      case e: Exception => 0
    }
  }
}
