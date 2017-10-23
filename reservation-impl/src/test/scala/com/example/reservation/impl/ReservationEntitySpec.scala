package com.example.reservation.impl

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

import akka.actor.ActorSystem
import com.example.common.Reservation
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import org.scalatest.{BeforeAndAfterAll, Inside, Matchers, WordSpec}

class ReservationEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll with Inside {

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

  "The reservation entity" should {

    "allow adding reservations" in withDriver { driver =>

      val reservation = Reservation(LocalDate.now, LocalDate.now.plus(2, ChronoUnit.DAYS))

      val outcome = driver.run(AddReservation(reservation))

      outcome.events should have size 1

      inside(outcome.events.head) {
        case added @ ReservationAdded(id, _, res) =>
          id should ===(listingId)
          res should ===(reservation)
          outcome.replies should contain only added
      }

      outcome.state should ===(ReservationState(Seq(reservation)))
    }

  }

}
