package com.example.reservation.impl

import akka.Done
import com.datastax.driver.extras.codecs.jdk8.LocalDateCodec
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
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
      ).build()
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
