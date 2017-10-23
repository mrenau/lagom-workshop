package com.example.reservation.impl

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

import akka.actor.ActorSystem
import com.example.common.Reservation
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

class ReservationEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll {

  private val system = ActorSystem("ReservationEntitySpec",
    JsonSerializerRegistry.actorSystemSetupFor(ReservationSerializerRegistry))

  override protected def afterAll(): Unit = {
    system.terminate()
  }

  private val listingId = UUID.randomUUID()

  private def withDriver(block: PersistentEntityTestDriver[ReservationCommand[_], ReservationEvent, ReservationState] => Unit) = {
    val driver = new PersistentEntityTestDriver(system, new ReservationEntity, listingId.toString)
    block(driver)
    driver.getAllIssues should ===(Nil)
  }


}
