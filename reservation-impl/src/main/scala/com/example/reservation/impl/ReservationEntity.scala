package com.example.reservation.impl

import java.util.UUID

import com.example.common.Reservation
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag}
import play.api.libs.json._

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

/**
  * Trait for all reservation commands.
  */
sealed trait ReservationCommand[R] extends ReplyType[R]

/**
  * Command to add a reservation.
  *
  * @param reservation The reservation to add.
  */
case class AddReservation(
  reservation: Reservation
) extends ReservationCommand[ReservationAdded]

object AddReservation {

  /**
    * JSON format for the add reservation command.
    *
    * Commands may be sent over the wire to entities because entites are
    * sharded across your Lagom cluster. This will be used to define
    * how the command gets serialized and deserialized.
    */
  implicit val format: Format[AddReservation] = Json.format
}

/**
  * Command to get the current reservations.
  */
case object GetCurrentReservations extends ReservationCommand[Seq[Reservation]] {

  /**
    * JSON format for the get current reservations command.
    */
  implicit val format: Format[GetCurrentReservations.type] =
    Format(Reads(_ => JsSuccess(GetCurrentReservations)), Writes(_ => JsString("get")))
}

/**
  * The state of the reservation listing.
  *
  * This is used to accumlate all the reservation events into a current state.
  *
  * @param reservations The reservations for this listing.
  */
case class ReservationState(reservations: Seq[Reservation])

object ReservationState {

  /**
    * JSON format for the reservation state.
    *
    * When reservation entities are snapshotted, the state is stored to the
    * snapshot store. This will be used to define how it gets serialized
    * and deserialized in that store.
    */
  implicit val format: Format[ReservationState] = Json.format

  /**
    * The empty state, no reservations.
    */
  val empty = ReservationState(Nil)
}

