package com.cart.routing

import com.cart.CreateCart
import com.cart.GetCartItems
import com.cart.RestMessage
import com.cart.core.CreateCartActor
import com.cart.core.GetCartItemsActor
import akka.actor.Actor
import akka.actor.Props
import spray.routing.Directive.pimpApply
import spray.routing.HttpService
import spray.routing.Route
import spray.routing.directives.ParamDefMagnet.apply
import com.cart.clients.CartsClient
import com.cart.core.GetCartsActor
import com.cart.GetCarts
import com.cart.core.AddItemToCartActor
import com.cart.AddItemToCart
import com.cart.RemoveItemFromCart
import com.cart.core.RemoveItemFromCartActor
import com.cart.core.DeleteCartActor
import com.cart.DeleteCart

/**Defines REST requests routing and their processing using per-request pattern. */
class RestRouting extends HttpService with Actor with PerRequestCreator {
  implicit def actorRefFactory = context

  def receive = runRoute(route)

  val cartsService = context.actorOf(Props[CartsClient], "carts-service")

  val route = {
    pathPrefix("carts") {
      get {
        pathEnd {
          carts(GetCarts())
        }
      } ~
        get {
          path(Segment) { cartId =>
            cartItems(GetCartItems(cartId))
          }
        } ~
        post {
          pathEnd {
            createCart(CreateCart())
          }
        } ~
        put {
          path(Segment / "items" / Segment) { (cartId, itemId) =>
            addItemToCart(AddItemToCart(cartId, itemId))
          }
        } ~
        delete {
          path(Segment) {cartId =>
            deleteCart(DeleteCart(cartId))
          }
        } ~
        delete {
          path(Segment/"items"/Segment) { (cartId, itemId) =>
            removeItemFromCart(RemoveItemFromCart(cartId, itemId))
          }
        }
    }
  }

  def deleteCart(message: RestMessage): Route =
    ctx => perRequest(ctx, Props(new DeleteCartActor(cartsService)), message)
  
  def removeItemFromCart(message: RestMessage): Route =
    ctx => perRequest(ctx, Props(new RemoveItemFromCartActor(cartsService)), message)
  
  def addItemToCart(message: RestMessage): Route =
    ctx => perRequest(ctx, Props(new AddItemToCartActor(cartsService)), message)

  def carts(message: RestMessage): Route =
    ctx => perRequest(ctx, Props(new GetCartsActor(cartsService)), message)

  def cartItems(message: RestMessage): Route =
    ctx => perRequest(ctx, Props(new GetCartItemsActor(cartsService)), message)

  def createCart(message: RestMessage): Route =
    ctx => perRequest(ctx, Props(new CreateCartActor(cartsService)), message)
}
