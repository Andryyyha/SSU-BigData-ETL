import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types.{DoubleType, IntegerType, StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Row, SparkSession}

object Main {
  case class AirCheck(date: String,
                      BEN: String,
                      CO: String,
                      EBE: String,
                      MXY: String,
                      NMHC: String,
                      NO_2: String,
                      NOx: String,
                      OXY: String,
                      O_3: String,
                      PM10: String,
                      PXY: String,
                      SO_2: String,
                      TCH: String,
                      TOL: String,
                      station: Int
                     )

  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkSession
      .builder()
      .appName("testApp")
      .config("spark.master", "local[2]")
      .getOrCreate()

    import spark.implicits._

    var data = spark.sparkContext.textFile("/Users/ilyichev/IdeaProjects/testAppScala/air-quality-madrid/csvs_per_year/csvs_per_year/madrid_2001.csv")
    val header: String = data.first()
    data = data.filter(row => row != header)

    val dataSplitted = data.map(x => x.split(","))

    val air: DataFrame = dataSplitted
      .map { case Array(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16)
      => AirCheck(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15,  r16.toInt)}
      .toDF()

    air.show()

  }

}




