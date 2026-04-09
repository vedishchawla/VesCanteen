package com.example.vescanteen

import com.example.vescanteen.model.CartItem
import com.example.vescanteen.model.MenuItem

/**
 * Singleton in-memory cart manager.
 * Acts like React Context — a global state holder for cart items.
 */
object CartManager {

    private val cartItems = mutableListOf<CartItem>()

    /** Add item to cart. If already exists, increment quantity. */
    fun addItem(menuItem: MenuItem) {
        val existing = cartItems.find { it.menuItem.id == menuItem.id }
        if (existing != null) {
            existing.quantity++
        } else {
            cartItems.add(CartItem(menuItem, 1))
        }
    }

    /** Remove one quantity. If quantity becomes 0, remove entirely. */
    fun removeItem(menuItemId: String) {
        val existing = cartItems.find { it.menuItem.id == menuItemId }
        if (existing != null) {
            existing.quantity--
            if (existing.quantity <= 0) {
                cartItems.remove(existing)
            }
        }
    }

    /** Remove an item completely from cart regardless of quantity. */
    fun deleteItem(menuItemId: String) {
        cartItems.removeAll { it.menuItem.id == menuItemId }
    }

    /** Get all cart items */
    fun getItems(): List<CartItem> = cartItems.toList()

    /** Get total price of all items */
    fun getTotal(): Double = cartItems.sumOf { it.getTotalPrice() }

    /** Get total item count (sum of quantities) */
    fun getItemCount(): Int = cartItems.sumOf { it.quantity }

    /** Clear all items from cart */
    fun clearCart() {
        cartItems.clear()
    }
}
