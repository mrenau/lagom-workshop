package com.example.reservation.impl

import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}

import scala.collection.immutable
import scala.concurrent.ExecutionContext

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
      .build()
  }

  /**
    * The tags this read side processor handles.
    */
  override def aggregateTags = immutable.Set(ReservationEvent.Tag)
}
