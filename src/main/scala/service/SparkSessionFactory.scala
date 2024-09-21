package service

import org.apache.spark.sql.SparkSession

object SparkSessionFactory {
  private val localFsSession = SparkSession
    .builder()
    .master("local[1]")
    .appName("SparkDemo")
    .config(
      "spark.sql.extensions",
      "org.apache.iceberg.spark.extensions.IcebergSparkSessionExtensions"
    )
    .config("spark.sql.catalog.spark_catalog", "org.apache.iceberg.spark.SparkSessionCatalog")
    .config("spark.sql.catalog.spark_catalog.type", "hive")
    .config("spark.sql.catalog.local", "org.apache.iceberg.spark.SparkCatalog")
    .config("spark.sql.catalog.local.type", "hadoop")
    .config("spark.sql.catalog.local.warehouse", "/tmp/warehoue")
    .config("spark.sql.defaultCatalog", "local")
    .getOrCreate()
  private val awsSparkSession = SparkSession
    .builder()
    .appName("IcebergSession")
    .config(
      "spark.sql.extensions",
      "org.apache.iceberg.spark.extensions.IcebergSparkSessionExtensions"
    )
    .config("spark.sql.catalog.glue_catalog", "org.apache.iceberg.spark.SparkCatalog")
    .config(
      "spark.sql.catalog.glue_catalog.warehouse",
      s"s3://warehouse-bucket/"
    ) //TODO: replace with appropriate data lake bucket
    .config(
      "spark.sql.catalog.glue_catalog.catalog-impl",
      "org.apache.iceberg.aws.glue.GlueCatalog"
    )
    .config("spark.sql.catalog.glue_catalog.io-impl", "org.apache.iceberg.aws.s3.S3FileIO")
    .config("fs.s3a.block.size", "128M")
    .config("spark.default.parallelism", "40")
    .config("spark.executor.instances", "20")
    .config("spark.sql.iceberg.read.parallelism", "20")
    .getOrCreate()

  def getSession(sessionType: SessionType.Value): SparkSession = {
    sessionType match {
      case SessionType.PROD_SESSION => awsSparkSession
      case _                        => localFsSession
    }
  }

}
