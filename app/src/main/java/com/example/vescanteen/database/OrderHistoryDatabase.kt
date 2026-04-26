package com.example.vescanteen.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room Database class — Singleton database instance.
 * Exp 7: SQLite database backed by Room ORM.
 */
@Database(entities = [OrderHistoryEntity::class], version = 1, exportSchema = false)
abstract class OrderHistoryDatabase : RoomDatabase() {

    abstract fun orderHistoryDao(): OrderHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: OrderHistoryDatabase? = null

        /** Get singleton database instance */
        fun getDatabase(context: Context): OrderHistoryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OrderHistoryDatabase::class.java,
                    "order_history_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
