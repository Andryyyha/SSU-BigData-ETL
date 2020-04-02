package util

import java.nio.charset.StandardCharsets

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.streaming.pubsub.SparkPubsubMessage

object Utils {

  val stationsSchema: StructType = StructType(
    List(
      StructField("id", StringType, nullable = true),
      StructField("name", StringType, nullable = true),
      StructField("address", StringType, nullable = true),
      StructField("lon", StringType, nullable = true),
      StructField("lat", StringType, nullable = true),
      StructField("elevation", IntegerType, nullable = true)
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

  def toDouble(r: String): Option[Double] = {
    try {
      Some(r.toDouble)
    } catch {
      case e: Exception => None
    }
  }

  def toInt(r: String): Option[Int] = {
    try {
      Some(r.toInt)
    } catch {
      case e: Exception => None
    }
  }
}
