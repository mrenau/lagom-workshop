package com.example.reservation.impl

import akka.Done
import com.datastax.driver.extras.codecs.jdk8.LocalDateCodec
import com.datastax.driver.core.BoundStatement
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, ReadSideProcessor}
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}

import scala.collection.immutable
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

/**
  * Read side processor that stores current reservations
  */
class ReservationEventProcessor(session: CassandraSession, readSide: CassandraReadSide)(implicit ec: ExecutionContext)
  extends ReadSideProcessor[ReservationEvent] {

  /**
    * Build the handler.
    */
  override def buildHandler() = {
    readSide.builder[ReservationEvent]("current-reservations")
      .setGlobalPrepare(createTable)
      .setPrepare(_ => session.underlying()
        // Register the Java 8 LocalDate codec
        .map(_.getCluster.getConfiguration.getCodecRegistry.register(LocalDateCodec.instance))
        .map(_ => Done)
      ).setEventHandler[ReservationAdded](insertReservation)
      .build()
  }

  /**
    * Insert a reservation.
    */
  private def insertReservation(event: EventStreamElement[ReservationAdded]): Future[immutable.Seq[BoundStatement]] = {
    session.prepare(
      """INSERT INTO current_reservations (listing_id, checkout, checkin, reservation_id)
        |VALUES (?, ?, ?, ?)
      """.stripMargin).map { statement =>
        immutable.Seq(statement.bind(
          event.event.listingId,
          event.event.reservation.checkout,
          event.event.reservation.checkin,
          event.event.reservationId
        ))
      }
  }

  /**
    * Create the current reservations table.
    */
  private def createTable(): Future[Done] = {
    session.executeCreateTable(
      """
        |CREATE TABLE IF NOT EXISTS current_reservations (
        |  listing_id UUID,
        |  checkout DATE,
        |  checkin DATE,
        |  reservation_id UUID,
        |  PRIMARY KEY (listing_id, checkout)
        |) WITH CLUSTERING ORDER BY (checkout ASC);
      """.stripMargin)
  }

  /**
    * The tags this read side processor handles.
    */
  override def aggregateTags = immutable.Set(ReservationEvent.Tag)
}
