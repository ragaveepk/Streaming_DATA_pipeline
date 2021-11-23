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

import scala.collection.mutable
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.sys.process._
import scala.util.{Failure, Success}

private class FileProcessor extends Actor {
  import FileProcessor._
  implicit val system: ActorSystem = context.system
  implicit val mat: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val config = ConfigFactory.load()
  val producerConfig = config.getConfig("akka.kafka.producer")
  val producerSettings = ProducerSettings(producerConfig, new StringSerializer, new StringSerializer)

  override def receive = {
      case Message.FileModified(param) =>
        handleModify(param)
        passToKafka()
      case Message.FileCreated(param) =>
        handleCreate(param)
      case Message.FileDeleted(param) =>
        handleDelete(param)
    }

  def passToKafka() = {
    val states = Map("AL" -> "Alabama", "AK" -> "Alaska")

    val produce: Future[Done] =
      Source(states)
        .map((value) => new ProducerRecord[String, String]("test", value._1, value._2))
        .runWith(Producer.plainSink(producerSettings))

    produce onComplete  {
      case Success(_) => println("Done"); system.terminate()
      case Failure(err) => println(err.toString); system.terminate()
    }
  }

  def countLines(file: String): Int = {
    val cmd = "find /v /c \"\" data\\" + file
    val exec = cmd.!!
    return "[0-9]+".r.findFirstIn(exec.split(":").last).get.toInt
  }

  def handleModify(file: String): Unit = {
    //val cmd = "find /v /c \"\" data\\test_log.txt"
    val count = countMap(file)
    val currCount = countLines(file)

    if (currCount != count) {
      countMap(file) = currCount
    }

    // TODO: Read file from line number count till currCount
    // TODO: Pass the lines read to kafka stream

    val diff = currCount - count
    println(s"Hello there $file!")
    println("ll " + diff  + " ll")
  }

  def handleCreate(file: String): Unit = {
    countMap += (file -> countLines(file))
    println("Create done " + file + " count: " + countMap(file))
  }

  def handleDelete(file: String): Unit = {
    countMap -= (file)
    println("Delete done " + file + " count: " + countMap(file))
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
