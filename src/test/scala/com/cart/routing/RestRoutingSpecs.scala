package com.cart.routing

import java.util.UUID

import org.scalatest.FlatSpec
import org.scalatest.Matchers

import com.cart.Cart
import com.cart.Carts
import com.cart.GetCarts
import com.cart.RestMessage

import akka.testkit.TestActorRef
import akka.testkit.TestProbe
import spray.routing.Route
import spray.testkit.ScalatestRouteTest

/** An example of a test testing REST routing */
class RestRoutingSpecs extends FlatSpec with ScalatestRouteTest with Matchers {
  val getCartsService = TestProbe()
  
  def restRouting = TestActorRef(new RestRouting() {
    override def carts(message: RestMessage): Route =
      ctx => perRequest(ctx, getCartsService.ref, message)
  })
  
  "RestRouting" should "get Carts" in {
    val getCarts = Get("/carts") ~> restRouting.underlyingActor.route

    getCartsService.expectMsg(GetCarts())
    val uuid = UUID.randomUUID().toString()
    getCartsService.reply(Carts(Seq(Cart(uuid))))

    getCarts ~> check {
      responseAs[String] should equal(s"""{"carts":[{"id":"$uuid"}]}""")
    }
  }
}