package org.cs441.proj

import akka.actor.{ActorRef, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds.{ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY}

object Driver {
  def main(args: Array[String]): Unit = {
    import FileWatcher._

    val fileNumber = args(0)

    args(0) match {
      case "1" => val fileNumber = "1"
      case _   => val fileNumber = "2"
    }

    val system = ActorSystem("mySystem")
    val config = ConfigFactory.load()
    val path = config.getString("akka.actors.path") + fileNumber

    val watcher = system.actorOf(Props(new FileWatcher(Paths.get(path), fileNumber)))

    watcher ! Initialize
  }
}
