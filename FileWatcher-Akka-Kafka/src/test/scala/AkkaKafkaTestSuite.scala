package org.cs441.proj

import com.typesafe.config.ConfigFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class AkkaKafkaTestSuite extends AnyFlatSpec with Matchers {
  val config = ConfigFactory.load()

  it should "check if the application.conf parameters work" in {
    val logTime = config.getString("Client.logTime")
    val dT = config.getString("Client.dT")
    val pattern = config.getString("Client.pattern")
    assert(!(logTime.isEmpty && dT.isEmpty && pattern.isEmpty))
  }
}
