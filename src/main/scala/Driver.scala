package org.cs441.proj

import akka.actor.{ActorRef, ActorSystem, Props}
import org.cs441.proj.FileWatcher.when

import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds.{ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY}

object Driver {

  def main(args: Array[String]): Unit = {
    val base = System.getProperty("user.dir")
    // initialize the actor instance
    val system = ActorSystem("mySystem")
    print("FILE: " + Paths.get(base, "data"))
    val watcher: ActorRef = system.actorOf(Props(new FileWatcher(Paths.get(base, "data"))))

    // send the register callback message for create/modify events
    watcher ! when(events = ENTRY_CREATE, ENTRY_MODIFY) {
      case (ENTRY_CREATE, file) => println(s"$file got created")
      case (ENTRY_MODIFY, file) => println(s"$file got modified")
    }
  }

}
