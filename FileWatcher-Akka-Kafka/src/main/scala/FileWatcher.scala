package org.cs441.proj
import akka.actor._

import java.nio.file.StandardWatchEventKinds.{ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY}
import java.nio.file.{Files, Path, WatchEvent}
import scala.collection.mutable

class FileWatcher(file: Path, fileNumber : String) extends ThreadFileMonitor(file) with Actor {
  import FileWatcher._

  val processor: ActorRef = context.actorOf(FileProcessor.props, "fileProcessor")
  //Files.lines()
  // MultiMap from Events to registered callbacks
  protected[this] val callbacks = newMultiMap[Event, Callback]

  // Override the dispatcher from ThreadFileMonitor to inform the actor of a new event
  override def dispatch(event: Event, file: Path) = self ! FileWatcher.Message.NewEvent(event, file)

  // Override the onException from the ThreadFileMonitor
  override def onException(exception: Throwable) = self ! Status.Failure(exception)

  // when actor starts, start the ThreadFileMonitor
  override def preStart() = super.start()

  // before actor stops, stop the ThreadFileMonitor
  override def postStop() = super.interrupt()

  override def receive = {
    case Initialize => self ! when(events = ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE) {
      case (ENTRY_CREATE, file) => {
        println(s"$file got created")
        processor ! FileProcessor.Message.FileCreated(file.toString, fileNumber)
      }

      case (ENTRY_MODIFY, file) => {
        println(s"$file got modified")
        processor ! FileProcessor.Message.FileModified(file.toString, fileNumber)
      }

      case (ENTRY_DELETE, file) => {
        println(s"$file got deleted")
        processor ! FileProcessor.Message.FileDeleted(file.toString, fileNumber)
      }
    }
    case FileWatcher.Message.NewEvent(event, target) if callbacks contains event =>
      callbacks(event) foreach {f => f(event -> target)}

    case FileWatcher.Message.RegisterCallback(events, callback) =>
      events foreach {event => callbacks.addBinding(event, callback)}

    case FileWatcher.Message.RemoveCallback(event, callback) =>
      callbacks.removeBinding(event, callback)
  }

  private[this] def newMultiMap[A, B]: mutable.MultiMap[A, B] =
    new mutable.HashMap[A, mutable.Set[B]] with mutable.MultiMap[A, B]
}

object FileWatcher {
  type Event = WatchEvent.Kind[Path]
  type Callback = PartialFunction[(Event, Path), Unit]

  sealed trait Message
  object Message {
    case class NewEvent(event: Event, file: Path) extends Message
    case class RegisterCallback(events: Seq[Event], callback: Callback) extends Message
    case class RemoveCallback(event: Event, callback: Callback) extends Message
  }

  case object Initialize
  // util to create a RegisterCallback message for the actor
  def when(events: Event*)(callback: Callback): Message = {
    Message.RegisterCallback(events.distinct, callback)
  }
}