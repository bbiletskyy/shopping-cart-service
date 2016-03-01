package com.cart.clients

import java.util.UUID
import com.cart._
import com.cart.clients.CartItemsClient._
import com.cart.clients.CartsClient._
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.PoisonPill
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy.Escalate

/**Actor managing shopping carts*/
class CartsClient extends Actor with ActorLogging {
  def receive = active(Map.empty)

  def active(carts: Map[String, ActorRef]): Receive = {
    case m: Create =>
      val cartId = UUID.randomUUID().toString()
      val cartActor = context.actorOf(Props(classOf[CartItemsClient], cartId))
      val newCarts = carts + (cartId -> cartActor)
      sender ! Created(Cart(cartId))
      context.become(active(newCarts))
    case Delete(cartId) =>
      carts.get(cartId) match {
        case None => sender ! Validation(s"Cart $cartId not found")
        case Some(cart) =>
          cart ! PoisonPill
          val newCarts = carts - cartId
          sender ! Deleted(cartId)
          context.become(active(newCarts))
      }
    case GetCartRef(cartId) =>
      carts.get(cartId) match {
        case None       => sender ! Validation(s"Cart $cartId not found")
        case Some(cart) => sender ! CartRef(cart)
      }
    case m: GetAllCarts =>
      val allCarts = carts.keys.map(id => Cart(id)).toSeq
      sender ! AllCarts(allCarts)
  }
}
object CartsClient {
  case class Create()
  case class Created(cart: Cart)
  case class Delete(cartId: String)
  case class Deleted(cartId: String)
  case class GetCartRef(id: String)
  case class CartRef(service: ActorRef)
  case class GetAllCarts()
  case class AllCarts(carts: Seq[Cart])
}

/**Actor managing shopping cart items*/
class CartItemsClient(cartId: String) extends Actor with ActorLogging {
  def receive = active(Map.empty)

  def active(items: Map[String, CartItem]): Receive = {
    case UpdateItem(itemId, delta) => updateItem(items, itemId, delta)
    case m: GetContent => sender ! Content(items.values.toSeq)
  }
  
  def updateItem(items: Map[String, CartItem], itemId: String, delta: Int) {
     val newItem = items.getOrElse(itemId, CartItem(itemId, 0)).increment(delta)
      if (newItem.count < 0) {
        sender ! Validation(s"Item $itemId not found cart $cartId")
      } else if (newItem.count == 0) {
        val newItems = items - newItem.name
        sender ! ItemUpdated(newItems.values.toSeq)
        context become active(newItems)
      } else {
        val newItems = items + (newItem.name -> newItem)
        sender ! ItemUpdated(newItems.values.toSeq)
        context become active(newItems)
      }
  }
}

object CartItemsClient {
  case class UpdateItem(itemId: String, deltaCount: Int)
  case class ItemUpdated(content: Seq[CartItem])
  case class GetContent()
  case class Content(content: Seq[CartItem])
} 