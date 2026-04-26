package com.example.vescanteen.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/**
 * Room DAO (Data Access Object) — Defines SQL queries for order history.
 * Exp 7: Room Database CRUD operations.
 */
@Dao
interface OrderHistoryDao {

    /** Insert a new order into local DB */
    @Insert
    fun insertOrder(order: OrderHistoryEntity)

    /** Get all orders, newest first */
    @Query("SELECT * FROM order_history ORDER BY timestamp DESC")
    fun getAllOrders(): List<OrderHistoryEntity>

    /** Get orders by status */
    @Query("SELECT * FROM order_history WHERE status = :status ORDER BY timestamp DESC")
    fun getOrdersByStatus(status: String): List<OrderHistoryEntity>

    /** Get total number of orders */
    @Query("SELECT COUNT(*) FROM order_history")
    fun getOrderCount(): Int

    /** Delete all orders */
    @Query("DELETE FROM order_history")
    fun clearHistory()
}
