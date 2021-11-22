package org.cs441.proj

import akka.actor.{ActorRef, ActorSystem, Props}

import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds.{ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY}

object Driver {

  def main(args: Array[String]): Unit = {
    import FileWatcher._
    import FileProcessor._
    val base = System.getProperty("user.dir")
    // initialize the actor instance
    val system = ActorSystem("mySystem")
    val watcher: ActorRef = system.actorOf(Props(new FileWatcher(Paths.get(base, "data"))))
    val processor: ActorRef = system.actorOf(Props(new FileProcessor()))

    // send the register callback message for create/modify events
    watcher ! when(events = ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE) {
      case (ENTRY_CREATE, file) => {
        processor ! onCreate(file.toString)
        println(s"$file got created")
      }

      case (ENTRY_MODIFY, file) => {
        processor ! onModify(file.toString)
        println(s"$file got modified")
      }

      case (ENTRY_DELETE, file) => {
        processor ! onDelete(file.toString)
        println(s"$file got deleted")
      }
    }
  }
}
