package com.example.search.impl

import java.time.LocalDate
import java.util.UUID

import akka.Done
import akka.actor.{ActorSystem, Props}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.example.search.api.{ListingSearchResult, SearchService}
import akka.pattern.ask
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import com.example.common.ReservationAdded
import com.example.reservation.api.ReservationService

import scala.concurrent.duration._

/**
  * Implementation of the SearchService.
  */
class SearchServiceImpl(actorSystem: ActorSystem, reservationService: ReservationService) extends SearchService {

  import SearchActor._
  private val searchActor = actorSystem.actorOf(Props[SearchActor])
  implicit val searchActorTimeout = Timeout(10.seconds)

  // Subscribe to the reservation service events
  reservationService.events.subscribe.atLeastOnce(Flow[ReservationAdded].mapAsync(1) {
    case added: ReservationAdded => (searchActor ? added).mapTo[Done]
  })

  override def searchListings(checkin: LocalDate, checkout: LocalDate) = ServiceCall { _ =>
    (searchActor ? Search(checkin, checkout)).mapTo[Seq[ListingSearchResult]]
  }

  override def listingName(listingId: UUID) = ServiceCall { _ =>
    (searchActor ? ListingName(listingId)).mapTo[String]
  }
}
