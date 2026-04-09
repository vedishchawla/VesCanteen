package com.example.vescanteen.model

/**
 * Represents a placed order saved to Firestore.
 */
data class Order(
    val orderId: String = "",
    val userId: String = "",
    val items: List<Map<String, Any>> = emptyList(),
    val totalPrice: Double = 0.0,
    val status: String = "confirmed",
    val tokenNumber: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
