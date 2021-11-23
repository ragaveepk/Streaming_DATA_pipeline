package org.cs441.proj

import akka.actor.{ActorRef, ActorSystem, Props}

import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds.{ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY}

object Driver {

  def main(args: Array[String]): Unit = {
    import FileWatcher._

    val base = System.getProperty("user.dir")
    // initialize the actor instance
    val system = ActorSystem("mySystem")

    val watcher = system.actorOf(Props(new FileWatcher(Paths.get(base, "data"))))

    watcher ! Initialize
  }
}
