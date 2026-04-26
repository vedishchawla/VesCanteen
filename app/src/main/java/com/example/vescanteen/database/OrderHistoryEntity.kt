package com.example.vescanteen.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity — Represents a saved order in local SQLite database.
 * Exp 7: Room Database for offline order history.
 */
@Entity(tableName = "order_history")
data class OrderHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tokenNumber: Int,
    val items: String,          // Comma-separated item names
    val totalPrice: Double,
    val paymentMethod: String,
    val status: String,
    val timestamp: Long
)
