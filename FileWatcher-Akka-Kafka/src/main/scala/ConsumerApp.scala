package org.cs441.proj

import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.scaladsl.Sink
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.common.serialization.StringDeserializer
import Utils.CreateLogger

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

/**
 * Factory for [[ConsumerApp]] instances
 * Run ConsumerApp to check whether the messages are placed
 * in the MSK kafka stream by the Actor
 * */
object ConsumerApp {
  // initialize logger
  val logger = CreateLogger(this.getClass)

  // initialize the ActorSystem, Materializer and ExecutionContext
  implicit val system: ActorSystem = ActorSystem("consumer-sys")
  implicit val mat: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  // load the config
  val config = ConfigFactory.load()
  val consumerConfig = config.getConfig("akka.kafka.consumer")

  // create the Kafka ConsumerSettings
  val consumerSettings = ConsumerSettings(consumerConfig,
    new StringDeserializer,
    new StringDeserializer
  ).withProperty("security.protocol", "SSL")
    .withProperty("ssl.truststore.location", "/tmp/kafka.client.truststore.jks")

  // initialize the consumer to subscribe to the topic
  val consume = Consumer
    .plainSource(consumerSettings, Subscriptions.topics("AWSKafkaTutorialTopic"))
    .runWith(Sink.foreach(println))

  // when the consumer completes, log and terminate
  consume onComplete  {
    case Success(_) => {
      logger.info("Consumer test success! " + "\n")
      system.terminate()
    }
    case Failure(err) => {
      logger.info("Consumer terminated with error: " + err.toString + "\n")
      system.terminate()
    }
  }
}
