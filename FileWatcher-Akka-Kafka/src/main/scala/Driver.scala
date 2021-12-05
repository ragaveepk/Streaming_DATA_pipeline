package org.cs441.proj

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import Utils.CreateLogger
import Actors.FileWatcher
import java.nio.file.Paths

class Driver

/**
 * Factory for [[Driver]] instances
 * The workflow starts here
 * */
object Driver {
  // initialize logger
  val logger = CreateLogger(this.getClass)

  /** Main Method - Triggers the gRPC or REST server based on command line input
   * @param args : Array[String] - command line input 1 or 2
   * @return Unit
   */
  def main(args: Array[String]): Unit = {
    import FileWatcher._

    // switch the actor based on args(0)
    // "1" - Actor in first instance
    // "2" - Actor in second instance
    val fileNumber = if (args.length == 0) "1" else args(0)

    // create a ConfigFactory to read conf file
    val config = ConfigFactory.load()

    // create an actor system
    val system = ActorSystem("mySystem")
    logger.info("Created an actor system, " + system.name + "\n")

    // construct the path to be monitored
    val path = config.getString("akka.actors.path") + fileNumber
    logger.info("Path to be monitored: " + path + "\n")

    // create a watcher actorref
    val watcher = system.actorOf(Props(new FileWatcher(Paths.get(path), fileNumber)))
    logger.info("Sending message to initialize the watcher actorref" + "\n")

    // send Initialize message to watcher
    watcher ! Initialize
  }
}
