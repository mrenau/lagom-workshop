package com.example.reservation.impl

import java.util.UUID

import com.example.common.Reservation
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag}
import play.api.libs.json.{Format, Json}

/**
  * Trait for all reservation events.
  */
sealed trait ReservationEvent extends AggregateEvent[ReservationEvent] {

  /**
    * The tag for each event.
    *
    * Tags allow creating a read side stream of all events with the given tag.
    */
  def aggregateTag = ReservationEvent.Tag
}

object ReservationEvent {

  /**
    * Each event has the same tag.
    */
  val Tag = AggregateEventTag[ReservationEvent]
}

/**
  * The reservation added event.
  *
  * @param listingId The listing the reservation was added to.
  * @param reservationId The id of the reservation.
  * @param reservation The reservation dates.
  */
case class ReservationAdded(
  listingId: UUID,
  reservationId: UUID,
  reservation: Reservation
) extends ReservationEvent

object ReservationAdded {

  /**
    * JSON format for the reservation added.
    *
    * This will be used to serialize and deserialize the event when it is
    * persisted to the event log.
    */
  implicit val format: Format[ReservationAdded] = Json.format
}
