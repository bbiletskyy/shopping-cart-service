package com.cart

import akka.io.IO
import spray.can.Http

import akka.actor.{Props, ActorSystem}
import com.cart.routing.RestRouting
/**Starts up the application*/
object Boot extends App {
  implicit val system = ActorSystem("shoping-cart-service")

  val serviceActor = system.actorOf(Props(new RestRouting), name = "rest-routing")

  system.registerOnTermination {
    system.log.info("Shopping cart shutting down.")
  }

  IO(Http) ! Http.Bind(serviceActor, "localhost", port = 8080)
}