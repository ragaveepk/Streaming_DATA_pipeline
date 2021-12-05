package org.cs441.proj

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class AkkaKafkaTestSuite extends AnyFlatSpec with Matchers {
  val config = ConfigFactory.load()

  it should "check if the actor system is created" in {
    val system = ActorSystem("testSystemName")
    assert(system.name == "testSystemName")
  }
}
