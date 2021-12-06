package org.cs441.proj
package Actors

import java.nio.file.StandardWatchEventKinds.{ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY}
import java.nio.file.{Files, Path, WatchEvent, WatchKey}
import scala.collection.convert.ImplicitConversions.`iterator asScala`

/**
 * Class [[ThreadFileMonitor]]
 * Inherit [[Thread]] class and implement
 * methods defined by the trait [[FileMonitor]]
 * This class will create Threads for Akka actor
 * */
class ThreadFileMonitor(val root: Path) extends Thread with FileMonitor {
  // create a Daemon and handle Exception
  setDaemon(true)
  setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler {
    override def uncaughtException(t: Thread, e: Throwable): Unit = onException(e)
  })

  // create a new WatchService on the specified path
  val service = root.getFileSystem.newWatchService()

  /** Override run method from Thread
   * @param None
   * @return Unit
   */
  override def run(): Unit = {
    Iterator.continually(service.take()).foreach(process)
  }

  /** Override interrupt method from Thread
   * @param None
   * @return Unit
   */
  override def interrupt(): Unit = {
    // close the service and call super.interrupt()
    service.close()
    super.interrupt()
  }

  /** Override start method from Thread
   * @param None
   * @return Unit
   */
  override def start(): Unit = {
    // check whether the specified path is a single file or a series of directories
    // directories - recursively watch
    // single file/folder - watch the file alone
    if (Files.isDirectory(root)) {
      watch(root, recursive=true)
    } else {
      watch(root.getParent)
    }
    super.start()
  }

  /** watch method - register the thread to watch the file/directory for changes
   * @param file: Path to be watched
   * @param recursive: Boolean describing whether to watch recursively or not
   * @return Unit
   */
  protected[this] def watch(file: Path, recursive: Boolean=false): Unit = {
    if (Files.isDirectory(file)) {
      file.register(service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY)
      if (recursive) {
        Files.list(file).iterator().foreach(f => watch(f, recursive))
      }
    }
  }

  /** reactTo method - helper method used by process()
   * @param target: Path
   * @return Boolean
   */
  protected[this] def reactTo(target: Path): Boolean = {
    return Files.isDirectory(root) || (root == target)
  }

  /** process method - process based on the event type
   * @param key: WatchKey object
   * @return Boolean
   */
  protected[this] def process(key: WatchKey): Boolean = {
    // switch based on the type of event
    key.pollEvents() forEach {
      case event: WatchEvent[Path] =>
        val target = event.context()
        if(reactTo(target)) {
          if (Files.isDirectory(root) && event.kind() == ENTRY_CREATE) {
            watch(root.resolve(target))
          }
          dispatch(event.kind(), target)
        }
      case event => onUnknownEvent(event)
    }
    return key.reset()
  }

  /** dispatch method - describes what to do when a new event occurs
   * @param eventType: EventType
   * @param file: Path
   * @return Unit
   */
  def dispatch(eventType: WatchEvent.Kind[Path], file: Path): Unit = {
    eventType match {
      case ENTRY_CREATE => onCreate(file)
      case ENTRY_MODIFY => onModify(file)
      case ENTRY_DELETE => onDelete(file)
    }
  }
}
