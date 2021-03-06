import org.apache.spark.streaming.kafka010._
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.receiver.Receiver
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import scala.collection.mutable.ListBuffer
import java.util.UUID

import HelperUtils.{Constants, SparkUtil, CreateLogger, ObtainConfigReference, AwsEmailService}
import HelperUtils.Constants.{sparkConfig, kafkaConfig}

/** 
* SparkAggregation
* 
* Functionality:
* - Connect to Kafka Stream
* - Aggregate the data using Spark
* - Send notification to the stakeholders about the warn and errors in the logs
*/
class SparkAggregation
object SparkAggregation extends App {
  val logger = CreateLogger(classOf[SparkAggregation])

  logger.info("Creating new Spark Configuration")
  val conf = new SparkConf()
                  .setAppName(sparkConfig.getString(Constants.SPARK_APP_NAME))
                  .setMaster(sparkConfig.getString(Constants.SPARK_MASTER))
  conf.set("spark.executor.memory", "4g")
  val threshold = 0

  logger.info("Creating new Spark Context")
  val sc = new SparkContext(conf)

  logger.info("Creating new Spark Streaming Context")
  val streamingContext = new StreamingContext(
    sc, 
    Seconds(sparkConfig.getInt(Constants.SPARK_STREAMING_WINDOW_DURATION))
  )

  // Create Kafka Params
  logger.info("Create new Kafka Params")
  val kafkaParams = Map[String, Object](
    ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> kafkaConfig.getString(Constants.KAFKA_BROKER),
    ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
    ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
    ConsumerConfig.GROUP_ID_CONFIG -> kafkaConfig.getString(Constants.KAFKA_GROUP_ID),
    "security.protocol"->"SSL",
    "ssl.truststore.location"-> "/tmp/kafka.client.truststore.jks",
  )

  logger.info("Create new Kafka Direct Stream")
  val kafkaStream = KafkaUtils.createDirectStream[String, String](
    streamingContext,
    LocationStrategies.PreferConsistent,
    ConsumerStrategies.Subscribe[String, String](Set(kafkaConfig.getString(Constants.KAFKA_TOPIC)), kafkaParams)
  )

  kafkaStream.foreachRDD { rdd =>
    if(!rdd.isEmpty()) {
      logger.info("Processing new RDD")
      // Aggregation
      val errorCount = rdd.map(record => record.value)
                          .map(SparkUtil.isError)
                          .reduce(SparkUtil.combinePartitions)
      val warnCount = rdd.map(record => record.value)
                          .map(SparkUtil.isWarning)
                          .reduce(SparkUtil.combinePartitions)
      val debugCount = rdd.map(record => record.value)
                          .map(SparkUtil.isDebug)
                          .reduce(SparkUtil.combinePartitions)
      val infoCount = rdd.map(record => record.value)
                          .map(SparkUtil.isInfo)
                          .reduce(SparkUtil.combinePartitions)
      
      logger.info("Aggregated counts: E:" + errorCount + " W:" + warnCount + " D:" + debugCount + " I:" + infoCount)
      //      if (errorCount>threshold){
//        HelperUtils.AwsEmailService.email(errorCount,warnCount)
//      }
     
      logger.info("Sending result to AWS Mail")
      if(errorCount > threshold || warnCount > threshold) AwsEmailService.email(errorCount,warnCount)

    }
  }

  // Start the Streaming Context.
  logger.info("Starting Spark Streaming Context")
  streamingContext.start()
  // Keep the streaming alive until its manually stopped
  streamingContext.awaitTermination()
  logger.info("Spark Streaming Context has been terminated")  
}