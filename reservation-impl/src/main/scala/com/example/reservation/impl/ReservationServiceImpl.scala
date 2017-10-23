package com.example.reservation.impl

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

import com.example.common.{Reservation, ReservationAdded}
import com.example.reservation.api.ReservationService
import com.lightbend.lagom.scaladsl.api.ServiceCall

import scala.concurrent.Future

/**
  * Implementation of the reservation service
  */
class ReservationServiceImpl extends ReservationService {

  /**
    * Reserve the given listing.
    */
  override def reserve(listingId: UUID) = ServiceCall { reservation =>
    Future.successful(ReservationAdded(listingId, UUID.randomUUID(), reservation))
  }

  /**
    * Get the current reservations for the given listing.
    */
  override def getCurrentReservations(listingId: UUID) = ServiceCall { _ =>
    Future.successful(Seq(
      Reservation(LocalDate.now, LocalDate.now.plus(2, ChronoUnit.DAYS))
    ))
  }
}
