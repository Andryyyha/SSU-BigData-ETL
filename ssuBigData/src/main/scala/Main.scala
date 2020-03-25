import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder
import com.amazonaws.services.kinesis.model.{GetRecordsRequest, GetShardIteratorRequest, ShardIteratorType}
import com.typesafe.config.ConfigFactory
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Milliseconds, StreamingContext}

object Main {

  def main(args: Array[String]): Unit = {

    val batchInterval = Milliseconds(2000)

    val kinesisConf = ConfigFactory.parseResources("application.conf")
    
    val appName = kinesisConf.getString("conf.appName")
    val streamName = kinesisConf.getString("conf.streamNameAirChecks")
    val endpointUrl = kinesisConf.getString("conf.endpointUrl")
    val region = kinesisConf.getString("conf.region")

    val conf = new SparkConf().setAppName(appName)
    conf.setIfMissing("spark.master", "local[*]")

    val kinesisClient = AmazonKinesisClientBuilder.defaultClient()

    val ssc = new StreamingContext(conf, batchInterval)
    val shard = kinesisClient.describeStream(streamName).getStreamDescription.getShards.get(0)

    val shardIterator = kinesisClient
      .getShardIterator(
        new GetShardIteratorRequest()
          .withStreamName(streamName)
          .withShardId(shard.getShardId)
          .withStartingSequenceNumber(shard.getSequenceNumberRange.getStartingSequenceNumber)
          .withShardIteratorType(ShardIteratorType.AT_SEQUENCE_NUMBER)
      ).getShardIterator

    println(shardIterator)

    val getRecordsRequest = new GetRecordsRequest

    getRecordsRequest.setShardIterator(shardIterator)
    getRecordsRequest.setLimit(25)

    val recordsResult = kinesisClient.getRecords(getRecordsRequest)
    val records = recordsResult.getRecords

    records.forEach(record => println(new String(record.getData.array())))


//    val stream = KinesisInputDStream
//      .builder
//      .streamingContext(ssc)
//      .checkpointAppName(appName)
//      .streamName(streamName)
//      .initialPositionInStream(InitialPositionInStream.LATEST)
//      .endpointUrl(endpointUrl)
//      .regionName(region)
//      .checkpointInterval(batchInterval)
//      .storageLevel(StorageLevel.MEMORY_AND_DISK_2)
//      .build()
//
//    val airCheckData = stream.map { byteArray =>
//      val Array(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16) = new String(byteArray).split(",")
//      AirCheck(r1, r2,  Utils.toDouble(r3),  Utils.toDouble(r4),  Utils.toDouble(r5),  Utils.toDouble(r6),
//        Utils.toDouble(r7),  Utils.toDouble(r8), Utils.toDouble(r9),  Utils.toDouble(r10), Utils.toDouble(r11),
//        Utils.toDouble(r12),  Utils.toDouble(r13),  Utils.toDouble(r14), Utils.toDouble(r15), Utils.toInt(r16))
//    }
//
//    val coPollution = airCheckData.filter(_.CO.get > 0)
//
//    coPollution.print(1)
//
//    println(s"dates with CO pollution > 0")
//    coPollution.map { data =>
//      println(s"Date is ${data.date} on station ${data.station}")
//    }
//
//    // CO pollution checks over the last 20 seconds
//    coPollution.window(Seconds(20)).foreachRDD { rdd =>
//      val spark = SparkSession.builder.config(rdd.sparkContext.getConf).getOrCreate()
//      import spark.implicits._
//      val hotSensorDF = rdd.toDF()
////      hotSensorDF.createOrReplaceTempView("air_checks")
//      hotSensorDF.show()
//    }

//    ssc.remember(Minutes(1))
//    ssc.start()
//    ssc.awaitTerminationOrTimeout(2000 * 10 )
  }

}




