package com.example.vescanteen

import android.content.Context
import android.content.SharedPreferences
import com.example.vescanteen.model.CartItem
import com.example.vescanteen.model.MenuItem
import org.json.JSONArray
import org.json.JSONObject

/**
 * Singleton cart manager with SharedPreferences persistence.
 *
 * Exp 5: Cart items are saved to SharedPreferences as JSON.
 * This means even if the app is killed, the cart is restored on next launch.
 *
 * Storage: SharedPreferences (key-value store backed by XML file on device)
 * Format: JSON array of cart items
 */
object CartManager {

    private val cartItems = mutableListOf<CartItem>()
    private var prefs: SharedPreferences? = null

    private const val PREF_NAME = "ves_canteen_cart"
    private const val KEY_CART_ITEMS = "cart_items"

    /** Initialize with context (call once from MainActivity) */
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        loadCartFromPrefs()
    }

    /** Add item to cart. If already exists, increment quantity. */
    fun addItem(menuItem: MenuItem) {
        val existing = cartItems.find { it.menuItem.id == menuItem.id }
        if (existing != null) {
            existing.quantity++
        } else {
            cartItems.add(CartItem(menuItem, 1))
        }
        saveCartToPrefs()
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
        saveCartToPrefs()
    }

    /** Remove an item completely from cart regardless of quantity. */
    fun deleteItem(menuItemId: String) {
        cartItems.removeAll { it.menuItem.id == menuItemId }
        saveCartToPrefs()
    }

    /** Get all cart items */
    fun getItems(): List<CartItem> = cartItems.toList()

    /** Get total price of all items */
    fun getTotal(): Double = cartItems.sumOf { it.getTotalPrice() }

    /** Get total item count (sum of quantities) */
    fun getItemCount(): Int = cartItems.sumOf { it.quantity }

    /** Get quantity of a specific item by ID */
    fun getItemQuantity(itemId: String): Int {
        return cartItems.find { it.menuItem.id == itemId }?.quantity ?: 0
    }

    /** Clear all items from cart */
    fun clearCart() {
        cartItems.clear()
        saveCartToPrefs()
    }

    /**
     * Exp 5: Save cart to SharedPreferences as JSON string.
     * SharedPreferences stores data as key-value pairs in an XML file
     * at /data/data/com.example.vescanteen/shared_prefs/ves_canteen_cart.xml
     */
    private fun saveCartToPrefs() {
        val jsonArray = JSONArray()
        for (item in cartItems) {
            val jsonObj = JSONObject().apply {
                put("id", item.menuItem.id)
                put("name", item.menuItem.name)
                put("price", item.menuItem.price)
                put("category", item.menuItem.category)
                put("imageUrl", item.menuItem.imageUrl)
                put("description", item.menuItem.description)
                put("isAvailable", item.menuItem.isAvailable)
                put("drawableResName", item.menuItem.drawableResName)
                put("quantity", item.quantity)
            }
            jsonArray.put(jsonObj)
        }
        prefs?.edit()?.putString(KEY_CART_ITEMS, jsonArray.toString())?.apply()
    }

    /**
     * Exp 5: Load cart from SharedPreferences.
     * Parses the JSON string back into CartItem objects.
     */
    private fun loadCartFromPrefs() {
        val json = prefs?.getString(KEY_CART_ITEMS, null) ?: return
        try {
            val jsonArray = JSONArray(json)
            cartItems.clear()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val menuItem = MenuItem(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    price = obj.getDouble("price"),
                    category = obj.optString("category", ""),
                    imageUrl = obj.optString("imageUrl", ""),
                    description = obj.optString("description", ""),
                    isAvailable = obj.optBoolean("isAvailable", true),
                    drawableResName = obj.optString("drawableResName", "")
                )
                val quantity = obj.getInt("quantity")
                cartItems.add(CartItem(menuItem, quantity))
            }
        } catch (e: Exception) {
            // If JSON is corrupted, start fresh
            cartItems.clear()
        }
    }
}
