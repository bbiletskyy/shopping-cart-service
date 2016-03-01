package com.cart

// Messages

trait RestMessage
trait RestCreated extends RestMessage
case class CreateCart() extends RestMessage
case class CartCreated(cart: Cart) extends RestCreated
case class GetCarts() extends RestMessage
case class Carts(carts: Seq[Cart]) extends RestMessage
case class GetCartItems(cartId: String) extends RestMessage
case class CartItems(items: Seq[CartItem]) extends RestMessage
case class AddItemToCart(cartId: String, itemId: String) extends RestMessage
case class ItemToCartAdded(cartId: String, itemId: String) extends RestMessage
case class RemoveItemFromCart(cartId: String, itemId: String) extends RestMessage
case class ItemFromCartRemoved(cartId: String, itemId: String) extends RestMessage
case class DeleteCart(cartId: String) extends RestMessage
case class CartDeleted(cartId: String) extends RestMessage


// Domain objects
case class Cart(id: String)

case class CartItem(name: String, count: Int) {
  def increment(delta: Int) = CartItem(this.name, this.count + delta)
}

case class Error(message: String)

case class Validation(message: String)

// Exceptions
case object IllegalCartItemException extends Exception("Can not add a gun to the cart. Guns are illegal.")
