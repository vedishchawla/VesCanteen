package com.example.vescanteen.model

/**
 * Represents a food item on the canteen menu.
 * Maps directly to a Firestore document in the "menuItems" collection.
 * drawableResName is used for bundled images (hardcoded items).
 */
data class MenuItem(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val imageUrl: String = "",
    val description: String = "",
    val isAvailable: Boolean = true,
    val drawableResName: String = ""
)
