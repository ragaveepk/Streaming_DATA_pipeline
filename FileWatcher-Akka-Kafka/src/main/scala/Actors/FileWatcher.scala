package org.cs441.proj
package Actors

import akka.actor._
import org.cs441.proj.Utils.CreateLogger

import java.nio.file.StandardWatchEventKinds.{ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY}
import java.nio.file.{Path, WatchEvent}
import scala.collection.mutable

/**
 * Class [[FileWatcher]]
 * Inherit [[ThreadFileMonitor]] class and implement
 * methods defined by the trait [[Actor]]
 * This describes FileWatcher actor
 * */
class FileWatcher(file: Path, fileNumber : String) extends ThreadFileMonitor(file) with Actor {
  import FileWatcher._

  // initialize logger
  val logger = CreateLogger(this.getClass)

  // create a processor actorref
  val processor: ActorRef = context.actorOf(FileProcessor.props, "fileProcessor")

  // create a MultiMap to map Events with registered callbacks
  protected[this] val callbacks = newMultiMap[Event, Callback]

  /** Override dispatcher from ThreadFileMonitor to inform the actor of a new event
   * @param None
   * @return Unit
   */
  override def dispatch(event: Event, file: Path): Unit = {
    self ! FileWatcher.Message.NewEvent(event, file)
  }

  /** Override onException from ThreadFileMonitor
   * @param None
   * @return Unit
   */
  override def onException(exception: Throwable): Unit = {
    self ! Status.Failure(exception)
  }

  /** Override preStart from Actor
   * @param None
   * @return Unit
   */
  override def preStart(): Unit = {
    super.start()
  }

  /** Override preStop from Actor
   * @param None
   * @return Unit
   */
  override def postStop(): Unit = {
    super.interrupt()
  }

  /** Override receive method from Actor
   * @param None
   * @return PartialFunction[Any, Unit]
   */
  override def receive: PartialFunction[Any, Unit] = {
    // when the Initialize message is received, switch based on the event
    // ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE - call processor actor
    case Initialize => self ! when(events = ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE) {
      case (ENTRY_CREATE, file) => {
        logger.info(s"$file got created" + "\n")
        processor ! FileProcessor.Message.FileCreated(file.toString, fileNumber)
      }

      case (ENTRY_MODIFY, file) => {
        logger.info(s"$file got modified" + "\n")
        processor ! FileProcessor.Message.FileModified(file.toString, fileNumber)
      }

      case (ENTRY_DELETE, file) => {
        logger.info(s"$file got deleted" + "\n")
        processor ! FileProcessor.Message.FileDeleted(file.toString, fileNumber)
      }
    }

    // when the message is NewEvent
    case FileWatcher.Message.NewEvent(event, target) if callbacks contains event =>
      callbacks(event) foreach {f => f(event -> target)}

    // when the message is RegisterCallback
    case FileWatcher.Message.RegisterCallback(events, callback) =>
      events foreach {event => callbacks.addBinding(event, callback)}

    // when the message is RemoveCallback
    case FileWatcher.Message.RemoveCallback(event, callback) =>
      callbacks.removeBinding(event, callback)
  }

  // MultiMap - mutable.HashMap to map Events with registered callbacks
  private[this] def newMultiMap[A, B]: mutable.MultiMap[A, B] =
    new mutable.HashMap[A, mutable.Set[B]] with mutable.MultiMap[A, B]
}

/**
 * Factory for [[FileWatcher]] instances
 * The workflow starts here
 * */
object FileWatcher {
  // set up initial actor states and behaviours
  type Event = WatchEvent.Kind[Path]
  type Callback = PartialFunction[(Event, Path), Unit]

  // create a trait and object for the Message
  // to define the messages for FileWatcher actor
  sealed trait Message
  object Message {
    case class NewEvent(event: Event, file: Path) extends Message
    case class RegisterCallback(events: Seq[Event], callback: Callback) extends Message
    case class RemoveCallback(event: Event, callback: Callback) extends Message
  }

  // define Initialize message class
  // triggers the actor, done through Driver
  case object Initialize

  // util to create a RegisterCallback message for the actor
  def when(events: Event*)(callback: Callback): Message = {
    Message.RegisterCallback(events.distinct, callback)
  }
}