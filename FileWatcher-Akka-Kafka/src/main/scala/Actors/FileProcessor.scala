package org.cs441.proj
package Actors

import akka.Done
import akka.actor.{Actor, ActorSystem, Props}
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.cs441.proj.Utils.CreateLogger

import java.io.IOException
import java.nio.file.{Files, Paths}
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.sys.process._
import scala.util.{Failure, Success}

/**
 * Class [[FileProcessor]]
 * implements methods defined by the trait [[Actor]]
 * This describes FileMonitor actor
 * */
private class FileProcessor extends Actor {
  // import from FileProcessor object
  import FileProcessor._

  // initialize logger
  val logger = CreateLogger(this.getClass)

  // initialize the ActorSystem, Materializer and ExecutionContext
  implicit val system: ActorSystem = context.system
  implicit val mat: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  // load the config
  val config = ConfigFactory.load()
  val dir = config.getString("akka.actors.path")

  // create the Kafka ProducerSettings
  val producerConfig = config.getConfig("akka.kafka.producer")
  val producerSettings = ProducerSettings(
    producerConfig,
    new StringSerializer,
    new StringSerializer
  ).withProperty("security.protocol", "SSL")
    .withProperty("ssl.truststore.location", "/tmp/kafka.client.truststore.jks")

  /** Override receive method from Actor
   * @param None
   * @return PartialFunction[Any, Unit]
   */
  override def receive: PartialFunction[Any, Unit] = {
    // handle create/modify/delete based on the message
    // received from the FileWatcher actor
    case Message.FileModified(file, fileNumber) => {
      val count = handleModify(file, fileNumber)
      passToKafka(file, fileNumber, count)
    }

    case Message.FileCreated(file, fileNumber) => {
      handleCreate(file, fileNumber)
      passToKafka(file, fileNumber, 0)
    }

    case Message.FileDeleted(file, fileNumber) => {
      handleDelete(file, fileNumber)
    }
  }

  /** passToKafka method - places the logs in the Kafka streams
   * @param file: name of the file
   * @param fileNumber: directory name in which the file resides
   * @param count: previous count before modifications
   * @return Unit
   */
  def passToKafka(file: String, fileNumber: String, count: Int): Unit = {
    logger.info(s"Placing modified lines from $file in the Kafka streams" + "\n")

    // construct the path to the modified file
    val path = Paths.get(dir + fileNumber, file)

    // kafka producer - place logs in the kafka streams
    try {
      val stream = Files.lines(Paths.get(path.toString)).skip(count)
      val l = stream.iterator().asScala.toList
      val produce: Future[Done] =
        Source(l)
          .map((value) => new ProducerRecord[String, String]("AWSKafkaTutorialTopic", file, value))
          .runWith(Producer.plainSink(producerSettings))

      // upon completion
      produce onComplete  {
        case Success(_) => {
          logger.info(s"Sent new logs in $file to the Kafka streams" + "\n")
        }
        case Failure(err) => {
          logger.info(err + "\n")
        }
      }
    }

    // catch block for producer
    catch {
      case e: IOException =>
        e.printStackTrace()
    }
  }

  /** countLines method - helper method to count the number of lines
   * @param file: name of the file
   * @param fileNumber: directory name in which the file resides
   * @return count: number of lines in the file
   */
  def countLines(file: String, fileNumber: String): Int = {
    // for Windows uncomment below line
    // val cmd = "find /v /c \"\" data\\" + file

    // execute linux command to count the number of lines
    val cmd = "wc -l " + dir + fileNumber + "/" + file
    val exec = cmd.!!

    // for Windows uncomment below line
    // return "[0-9]+".r.findFirstIn(exec.split(":").last).get.toInt

    return "[0-9]+".r.findFirstIn(exec).get.toInt
  }

  /** handleModify method - method to handle if a file gets modified
   * @param file: name of the file
   * @param fileNumber: directory name in which the file resides
   * @return count: number of lines in the file after modification
   */
  def handleModify(file: String, fileNumber: String): Int = {
    // get the previous count from the initial state
    val count = countMap(file)

    // get the current count
    val currCount = countLines(file, fileNumber)

    // update the state if necessary
    if (currCount != count) {
      countMap(file) = currCount
    }

    // difference between state before and after modification
    val diff = currCount - count
    logger.info(s"$diff lines have been added to the $file" + "\n")

    // return old count
    return count
  }

  /** handleCreate method - method to handle if a file gets created
   * @param file: name of the file
   * @param fileNumber: directory name in which the file resides
   * @return count: number of lines in the file after modification
   */
  def handleCreate(file: String, fileNumber: String): Unit = {
    // add a new entry to the state
    countMap += (file -> countLines(file, fileNumber))
    logger.info("File " + file + " is created in folder " + fileNumber +  " with count: " + countMap(file) + "\n")
  }

  /** handleModify method - method to handle if a file gets deleted
   * @param file: name of the file
   * @param fileNumber: directory name in which the file resides
   * @return count: number of lines in the file after modification
   */
  def handleDelete(file: String, fileNumber: String): Unit = {
    // remove the entry corresponding to deleted file from the state
    countMap -= (file)
  }
}

/**
 * Factory for [[FileProcessor]] instances
 * The workflow starts here
 * */
object FileProcessor {
  // set up initial actor states and behaviours
  val props = Props[FileProcessor]
  val countMap = new mutable.HashMap[String, Int]() { override def default(key: String) = 0 }

  // create a trait and object for the Message
  // to define the messages for FileProcessor actor
  sealed trait Message
  object Message {
    final case class FileModified(file: String, fileNumber: String) extends Message
    final case class FileCreated(file: String, fileNumber: String) extends Message
    final case class FileDeleted(file: String, fileNumber: String) extends Message
  }
}