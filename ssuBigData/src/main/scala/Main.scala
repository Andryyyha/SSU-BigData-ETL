import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.kinesis.AmazonKinesisClient
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream
import com.typesafe.config.ConfigFactory
import org.apache.spark.rdd.RDD
import org.apache.spark.SparkConf
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.types.{DoubleType, IntegerType, StringType, StructField, StructType}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.kinesis.KinesisInputDStream
import org.apache.spark.streaming.{Milliseconds, Seconds, StreamingContext}

object Main {

  val schema = StructType(
    List(
      StructField("date", StringType, nullable = true),
      StructField("BEN", DoubleType, nullable = true),
      StructField("CO", DoubleType, nullable = true),
      StructField("EBE", DoubleType, nullable = true),
      StructField("MXY", DoubleType, nullable = true),
      StructField("NMHC", DoubleType, nullable = true),
      StructField("NO_2", DoubleType, nullable = true),
      StructField("NOx", DoubleType, nullable = true),
      StructField("OXY", DoubleType, nullable = true),
      StructField("O_3", DoubleType, nullable = true),
      StructField("PM10", DoubleType, nullable = true),
      StructField("PXY", DoubleType, nullable = true),
      StructField("SO_2", DoubleType, nullable = true),
      StructField("TCH", DoubleType, nullable = true),
      StructField("TOL", DoubleType, nullable = true),
      StructField("station", IntegerType, nullable = true)
    )
  )

  def extractAir(input: RDD[Array[Byte]]) = {
    input.map(w => Row.fromSeq(new String(w).split(",")))
//    input
//      .filter(_.length != 0)
//      .map(attribute => Row.fromSeq(attribute))
  }

  def main(args: Array[String]) : Unit = {
    val batchInterval = Milliseconds(2000)

    val kinesisConf = ConfigFactory.parseResources("application.conf")

    val appName = kinesisConf.getString("conf.appName")
    val streamName = kinesisConf.getString("conf.streamNameAirChecks")
    val endpointUrl = kinesisConf.getString("conf.endpointUrl")
    val region = kinesisConf.getString("conf.region")

    val conf = new SparkConf().setMaster("local[2]").setAppName(appName)
    val ssc = new StreamingContext(conf, batchInterval)

    val credentials = new DefaultAWSCredentialsProviderChain().getCredentials()
    val kinesisClient = new AmazonKinesisClient(credentials)
    kinesisClient.setEndpoint(endpointUrl)
    val numShards = kinesisClient.describeStream(streamName).getStreamDescription().getShards().size
    val numStreams = numShards

//    val kinesisStreams = (0 until numStreams).map { i =>
      val kinesisStreams =  KinesisInputDStream.builder
        .streamingContext(ssc)
        .endpointUrl(endpointUrl)
        .regionName(region)
        .streamName(streamName)
        .checkpointAppName(appName)
        .storageLevel(StorageLevel.MEMORY_AND_DISK_2)
        .build()
//    }

//    val unionStreams = ssc.union(kinesisStreams)

    val spark: SparkSession = SparkSession
      .builder()
      .appName(appName)
      .config(conf)
      .getOrCreate()

//    val Seq(projectID, windowInterval, slidingInterval, totalRunningTime) = args.toSeq

    kinesisStreams.foreachRDD {
        rdd =>
          import spark.implicits._
//          val crashDF = rdd.toDF()
          val crashDF = spark.createDataFrame(extractAir(rdd), schema)
            .cache()

          crashDF.show()
      }

    ssc.start()
    ssc.awaitTerminationOrTimeout(2000 * 10 )
  }
}