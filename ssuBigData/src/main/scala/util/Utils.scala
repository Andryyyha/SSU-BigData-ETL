package util

import java.nio.charset.StandardCharsets

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{DoubleType, IntegerType, StringType, StructField, StructType}
import org.apache.spark.streaming.pubsub.SparkPubsubMessage

object Utils {

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
      .map(_.split(""",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"""))
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
