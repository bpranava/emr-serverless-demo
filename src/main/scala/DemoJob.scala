import org.apache.spark.sql.SaveMode
import service.{SessionType, SparkSessionFactory}

object DemoJob {
  private val inputFilePath =
    "s3://input-bucket/sample_flower_data.csv" //If you want to test the job locally use path src/main/resources/sample_flower_data.csv
  private val tableName   = "flower_data"
  private val database    = "demo"
  private val catalogName = "glue_catalog"

  private def runJob(): Unit = {
    val spark = SparkSessionFactory.getSession(
      SessionType.PROD_SESSION
    ) // If you want to test the job locally use SessionType.LOCAL_SESSION
    val df = spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .format("csv")
      .load(inputFilePath);
    //Feel free to add all spark transforms here
    df.write.mode(SaveMode.Overwrite).saveAsTable(generateTableName())
    spark.close()
  }

  private def generateTableName(): String = {
    "%s.%s.%s".format(catalogName, database, tableName)
  }

  def main(args: Array[String]): Unit = {
    runJob()
  }
}
