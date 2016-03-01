package com.cart.core

import com.cart.AddItemToCart
import com.cart.CartCreated
import com.cart.CartDeleted
import com.cart.CartItems
import com.cart.Carts
import com.cart.CreateCart
import com.cart.DeleteCart
import com.cart.GetCartItems
import com.cart.GetCarts
import com.cart.IllegalCartItemException
import com.cart.ItemFromCartRemoved
import com.cart.ItemToCartAdded
import com.cart.RemoveItemFromCart
import com.cart.Validation
import com.cart.clients.CartItemsClient.Content
import com.cart.clients.CartItemsClient.GetContent
import com.cart.clients.CartItemsClient.ItemUpdated
import com.cart.clients.CartItemsClient.UpdateItem
import com.cart.clients.CartsClient.AllCarts
import com.cart.clients.CartsClient.CartRef
import com.cart.clients.CartsClient.Create
import com.cart.clients.CartsClient.Created
import com.cart.clients.CartsClient.Delete
import com.cart.clients.CartsClient.Deleted
import com.cart.clients.CartsClient.GetAllCarts
import com.cart.clients.CartsClient.GetCartRef
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy.Escalate
import akka.actor.actorRef2Scala
import com.cart.Validation
import com.cart.Validation

/**Actor processing a new cart creation request*/
class CreateCartActor(cartService: ActorRef) extends Actor with ActorLogging {
  def receive = {
    case m: CreateCart => cartService ! Create()
    case Created(cart) => context.parent ! CartCreated(cart)
  }
}
/**Actor processing retrieval of cart items request*/
class GetCartItemsActor(cartService: ActorRef) extends Actor with ActorLogging {
  def receive = {
    case GetCartItems(id) =>
      cartService ! GetCartRef(id)
      context become waitingForResponse(id)
  }
  def waitingForResponse(cartId: String): Receive = {
    case CartRef(cartActor) => cartActor ! GetContent()
    case Content(items)     => context.parent ! CartItems(items)
    case v: Validation      => context.parent ! v
  }
}
/**Actor responsible for retrieval of carts*/
class GetCartsActor(cartService: ActorRef) extends Actor with ActorLogging {
  def receive = {
    case m: GetCarts     => cartService ! GetAllCarts()
    case AllCarts(carts) => context.parent ! Carts(carts)
  }
}

/**Actor responsible for adding an item to a cart*/
class AddItemToCartActor(cartService: ActorRef) extends Actor with ActorLogging {
  def receive = {
    case AddItemToCart(cartId, itemId) => addItemToCart(cartId, itemId)
  }
  def waitingForResponse(cartId: String, itemId: String): Receive = {
    case CartRef(cart)        => cart ! UpdateItem(itemId, 1)
    case ItemUpdated(content) => context.parent ! ItemToCartAdded(cartId, itemId)
    case v: Validation        => context.parent ! v
  }
  def addItemToCart(cartId: String, itemId: String) {
    if (itemId.equalsIgnoreCase("gun")) {
      throw IllegalCartItemException
    }
    cartService ! GetCartRef(cartId)
    context become waitingForResponse(cartId, itemId)
  }
  override val supervisorStrategy =
    OneForOneStrategy() {
      case _ => Escalate
    }
}

/**Actor responsible for removing an item from a cart*/
class RemoveItemFromCartActor(cartService: ActorRef) extends Actor with ActorLogging {
  def receive = {
    case RemoveItemFromCart(cartId, itemId) =>
      cartService ! GetCartRef(cartId)
      context become waitingForResponse(cartId, itemId)
  }
  def waitingForResponse(cartId: String, itemId: String): Receive = {
    case CartRef(cart)        => cart ! UpdateItem(itemId, -1)
    case ItemUpdated(content) => context.parent ! ItemFromCartRemoved(cartId, itemId)
    case v: Validation        => context.parent ! v
  }
}
/**Actor responsible for deleting a cart*/
class DeleteCartActor(cartService: ActorRef) extends Actor with ActorLogging {
  def receive = {
    case DeleteCart(cartId) => cartService ! Delete(cartId)
    case Deleted(cartId)    => context.parent ! CartDeleted(cartId)
    case v: Validation      => context.parent ! v
  }
}
