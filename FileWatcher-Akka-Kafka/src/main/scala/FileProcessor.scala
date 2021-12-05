package org.cs441.proj

import akka.Done
import akka.actor.{Actor, ActorSystem, Props}
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

import java.nio.file.Paths
import scala.collection.mutable
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.sys.process._
import scala.util.{Failure, Success}
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.{Collectors, Stream}
import scala.collection.JavaConverters._

private class FileProcessor extends Actor {
  import FileProcessor._
  implicit val system: ActorSystem = context.system
  implicit val mat: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val config = ConfigFactory.load()
  val dir = config.getString("akka.actors.path")
  val producerConfig = config.getConfig("akka.kafka.producer")
  val producerSettings = ProducerSettings(
    producerConfig,
    new StringSerializer,
    new StringSerializer
  ).withProperty("security.protocol", "SSL")
    .withProperty("ssl.truststore.location", "/tmp/kafka.client.truststore.jks")

  override def receive = {
      case Message.FileModified(file, fileNumber) =>
      val count = handleModify(file, fileNumber)
      passToKafka(file, fileNumber, count)
    case Message.FileCreated(file, fileNumber) =>
      handleCreate(file, fileNumber)
      passToKafka(file, fileNumber, 0)
    case Message.FileDeleted(file, fileNumber) =>
      handleDelete(file, fileNumber)
  }

  def passToKafka(file: String, fileNumber: String, count: Int) = {
    println("Inside pass to kafka... " + count)
    //val base = System.getProperty("user.dir")
    val path = Paths.get(dir + fileNumber, file)
    try {
      val stream = Files.lines(Paths.get(path.toString)).skip(count)
      val l = stream.iterator().asScala.toList
      l.foreach(println);
      val produce: Future[Done] =
        Source(l)
          .map((value) => new ProducerRecord[String, String]("AWSKafkaTutorialTopic", file, value))
          .runWith(Producer.plainSink(producerSettings))

      produce onComplete  {
        case Success(_) => println("Done")
        case Failure(err) => println(err.toString)
      }
    }
    catch {
      case e: IOException =>
        e.printStackTrace()
    }
  }

  def countLines(file: String, fileNumber: String): Int = {
    //val cmd = "find /v /c \"\" data\\" + file
    val cmd = "wc -l " + dir + fileNumber + "/" + file
    val exec = cmd.!!
    //return "[0-9]+".r.findFirstIn(exec.split(":").last).get.toInt
    return "[0-9]+".r.findFirstIn(exec).get.toInt
  }

  def handleModify(file: String, fileNumber: String): Int = {
    //val cmd = "find /v /c \"\" data\\test_log.txt"
    val count = countMap(file)
    val currCount = countLines(file, fileNumber)

    if (currCount != count) {
      countMap(file) = currCount
    }

    // TODO: Read file from line number count till currCount
    // TODO: Pass the lines read to kafka stream

    val diff = currCount - count
    //println(s"Hello there $file!")
    //println("ll: " + diff  + " :ll")
    return count
  }

  def handleCreate(file: String, fileNumber: String): Unit = {
    countMap += (file -> countLines(file))
    println("File " + file + " is created in folder " + fileNumber +  " with count: " + countMap(file))
  }

  def handleDelete(file: String): Unit = {
    countMap -= (file)
    println("File " + file + " was deleted in folder " + fileNumber)
  }
}

object FileProcessor {
  val props = Props[FileProcessor]
  val countMap = new mutable.HashMap[String, Int]() { override def default(key: String) = 0 }

  // “messages” that FileProcessor can handle
  sealed trait Message
  object Message {
    final case class FileModified(param: String) extends Message
    final case class FileCreated(param: String) extends Message
    final case class FileDeleted(param: String) extends Message
  }
}