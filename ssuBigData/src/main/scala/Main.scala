import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.pubsub.{PubsubUtils, SparkGCPCredentials}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import Metrics.startMetrics

object Main {

  def main(args: Array[String]) : Unit = {
    if (args.length != 4) {
      System.err.println(
        """
          | Usage: TrendingHashtags <projectID> <windowLength> <slidingInterval> <totalRunningTime>
          |
          |     <projectID>: ID of Google Cloud project
          |     <windowLength>: The duration of the window, in seconds
          |     <slidingInterval>: The interval at which the window calculation is performed, in seconds.
          |     <totalRunningTime>: Total running time for the application, in minutes. If 0, runs indefinitely until termination.
          |
        """.stripMargin)
      System.exit(1)
    }

    val Seq(projectID, windowInterval, slidingInterval, totalRunningTime) = args.toSeq
    val appName = "ssuBigData"
    val conf = new SparkConf().setMaster("local[2]").setAppName(appName)
    val ssc = new StreamingContext(conf, Seconds(slidingInterval.toInt))

    val spark: SparkSession = SparkSession
      .builder()
      .appName(appName)
      .config(conf)
      .getOrCreate()

    val stationsStream = PubsubUtils
      .createStream(
        ssc,
        projectID,
        None,
        "stations_pull",
        SparkGCPCredentials.builder.build(), StorageLevel.MEMORY_AND_DISK_SER_2)

    startMetrics(stationsStream, windowInterval.toInt, slidingInterval.toInt, spark)

    ssc.start()

    if (totalRunningTime.toInt == 0) {
      ssc.awaitTermination()
    }
    else {
      ssc.awaitTerminationOrTimeout(1000 * 60 * totalRunningTime.toInt)
    }
  }
}