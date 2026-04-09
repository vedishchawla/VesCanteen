package com.example.vescanteen.model

/**
 * Represents an item in the user's cart.
 * Wraps a MenuItem with a quantity.
 */
data class CartItem(
    val menuItem: MenuItem,
    var quantity: Int = 1
) {
    /** Total price for this cart entry */
    fun getTotalPrice(): Double = menuItem.price * quantity
}
