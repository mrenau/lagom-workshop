package com.example.reservation.impl

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

import com.example.common
import com.example.reservation.api.ReservationService
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

import scala.concurrent.Future

/**
  * Implementation of the reservation service
  */
class ReservationServiceImpl(persistentEntityRegistry: PersistentEntityRegistry) extends ReservationService {

  /**
    * Reserve the given listing.
    */
  override def reserve(listingId: UUID) = ServiceCall { reservation =>
    Future.successful(common.ReservationAdded(listingId, UUID.randomUUID(), reservation))
  }

  /**
    * Get the current reservations for the given listing.
    */
  override def getCurrentReservations(listingId: UUID) = ServiceCall { _ =>
    Future.successful(Seq(
      common.Reservation(LocalDate.now, LocalDate.now.plus(2, ChronoUnit.DAYS))
    ))
  }
}
