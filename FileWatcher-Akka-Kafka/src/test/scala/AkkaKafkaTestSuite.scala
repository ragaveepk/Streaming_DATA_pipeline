package org.cs441.proj

import akka.actor.{ActorRef, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import Actors.FileWatcher
import Utils.CreateLogger

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.slf4j.Logger

import java.nio.file.Paths

class AkkaKafkaTestSuite extends AnyFlatSpec with Matchers {
  val config = ConfigFactory.load()

  it should "check if the actor system is created" in {
    val system = ActorSystem("testSystem")
    assert(system.name == "testSystem")
  }

  it should "check FileWatcher ActorRef" in {
    val system = ActorSystem("testSystem")
    val watcher = system.actorOf(Props(new FileWatcher(Paths.get("test"), "1")))
    assert(watcher.isInstanceOf[ActorRef])
  }

  it should "check logger type" in {
    val logger = CreateLogger(this.getClass)
    assert(logger.isInstanceOf[Logger])
  }

  it should "check bootstrapServer name" in {
    val bootstrapServerShouldBe = "b-1.awskafkacluster1.7x55t0.c17.kafka.us-east-1.amazonaws.com:9094,b-2.awskafkacluster1.7x55t0.c17.kafka.us-east-1.amazonaws.com:9094"
    val bootstrapServer = config.getString("akka.kafka.producer.kafka-clients.bootstrap.servers")
    assert(bootstrapServer == bootstrapServerShouldBe)
  }

  it should "check the path to be monitored" in {
    val pathShouldBe = "/mnt/efs/fs1/"
    val path = config.getString("akka.actors.path")
    assert(path == pathShouldBe)
  }
}
