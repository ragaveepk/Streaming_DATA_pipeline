package org.cs441.proj

import akka.actor.Actor

import java.nio.file.Paths
import scala.collection.mutable
import scala.sys.process._

private class FileProcessor extends Actor {
  import FileProcessor._
  override def receive = {
      case Message.FileModified(param) =>
        handleModify(param)
      case Message.FileCreated(param) =>
        handleCreate(param)
      case Message.FileDeleted(param) =>
        handleDelete(param)
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
  val countMap = new mutable.HashMap[String, Int]() { override def default(key: String) = 0 }
  // “messages” that FileProcessor can handle
  sealed trait Message
  object Message {
    final case class FileModified(param: String) extends Message
    final case class FileCreated(param: String) extends Message
    final case class FileDeleted(param: String) extends Message

  }

  def onModify(param: String): Message = {
    Message.FileModified(param)
  }

  def onCreate(param: String): Message = {
    Message.FileCreated(param)
  }

  def onDelete(param: String): Message = {
    Message.FileDeleted(param)
  }
}
