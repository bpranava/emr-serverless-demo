
# EMR Serverless Demo

This repository contains a Scala-based project that demonstrates how to use Amazon EMR Serverless for running Spark jobs. The project is designed to showcase an end-to-end setup, from configuring a Spark session to executing jobs in an EMR serverless environment.

## Project Structure

```
emr-serverless-demo/
│
├── src/
│   └── main/
│       ├── resources/       # Contains resource files (e.g., configs)
│       └── scala/           # Scala source code for the application
│           └── service/
│               ├── SessionType.scala       # Defines session types for Spark
│               ├── SparkSessionFactory.scala # Factory for creating Spark sessions
│           └── DemoJob.scala           # Main job to be executed
│
├── build.gradle             # Gradle build configuration
├── settings.gradle          # Project settings
├── .scalafmt.conf           # Code formatting configuration
├── gradlew                  # Gradle wrapper script for UNIX
├── gradlew.bat              # Gradle wrapper script for Windows
└── .gitignore               # Git ignore file
```

## Getting Started

### Prerequisites

- JDK 11 or 17
- Scala 2.12.19
- Gradle
- AWS Account with permissions to use EMR serverless

### Setup

1. Clone the repository:

    ```bash
    git clone https://github.com/your-username/emr-serverless-demo.git
    cd emr-serverless-demo
    ```

2. Install dependencies:
    ```bash
    ./gradlew build
    ```

3. Modify AWS configurations as needed for your environment.

### Running the Application

To run the Spark job locally or in an EMR serverless environment:

```bash
scala -J-Xmx10g -classpath build/libs/emr-serverless-demo-all.jar DemoJob 
```

For EMR serverless, you will need to package the job and submit it through the AWS Management Console or via AWS CLI.

### Key Components

- **`SessionType.scala`**: Defines various spark session session types for Spark jobs.
- **`SparkSessionFactory.scala`**: A factory class to create Spark sessions, customized for EMR serverless.
- **`DemoJob.scala`**: The main spark job class .

### Deployment

Follow these steps to deploy and run your Spark job on AWS EMR Serverless:

1. Package your Spark application:
    ```bash
    ./gradlew shadowJar
    ```

2. Upload the resulting JAR file to an S3 bucket.

3. Use the AWS CLI or AWS Console to submit the job to EMR Serverless:

   ```bash
      aws emr-serverless create-application \
                 --name my-serverless-emr-application \
                 --release-label emr-7.1.0 \
                 --type SPARK \
                 --network-configuration "{
                     \"securityGroupIds\": [\"sg-1\",\"sg-2\"],
                     \"subnetIds\": [\"subnet-1\",\"subnet-2\"]
                 }" \
                 --monitoring-configuration '{
                     "managedPersistenceMonitoringConfiguration": {
                         "enabled": true
                     },
                     "cloudWatchLoggingConfiguration": {
                         "enabled": true,
                         "logTypes": {
                           "SPARK_DRIVER": ["stdout", "stderr"]
                         }
                     }
                 }'
   ```
    Use the returned application id to execute the below command
   ```bash
   aws emr-serverless start-job-run \
    --application-id "APPLICATION_ID" \
    --execution-role-arn "emr_role_arn" \
    --job-driver '{
        "sparkSubmit": {
            "entryPoint": "s3://jar-bucket/jar-path/emr-serverless-demo-all.jar",
            "entryPointArguments": "",
            "sparkSubmitParameters": "--class DemoJob \
                --executor-cores=4 \
                --conf spark.driver.cores=4 \
                --conf spark.executor.memory=12G \
                --conf spark.driver.memory=8G \
                --conf spark.dynamicAllocation.initialExecutors=4 \
                --conf spark.dynamicAllocation.minExecutors=2 \
                --conf spark.dynamicAllocation.maxExecutors=30 \
                --conf spark.hadoop.hive.metastore.client.factory.class=com.amazonaws.glue.catalog.metastore.AWSGlueDataCatalogHiveClientFactory \
                --conf spark.emr-serverless.driverEnv.JAVA_HOME=/usr/lib/jvm/java-17-amazon-corretto.x86_64/ \
                --conf spark.executorEnv.JAVA_HOME=/usr/lib/jvm/java-17-amazon-corretto.x86_64/"
        }
    }' \
    --configuration-overrides '{
        "monitoringConfiguration": {
            "managedPersistenceMonitoringConfiguration": {
                "enabled": true
            },
            "cloudWatchLoggingConfiguration": {
                "enabled": true,
                "logTypes": {
                    "SPARK_DRIVER": ["stdout", "stderr"]
                }
            }
        }
    }'
   ```

