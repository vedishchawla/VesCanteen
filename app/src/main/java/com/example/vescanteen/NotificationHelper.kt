package com.example.vescanteen

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * Helper class to show local notifications.
 * Used for order confirmations and cart reminders.
 */
object NotificationHelper {

    private const val CHANNEL_ID = "ves_canteen_orders"
    private const val CHANNEL_NAME = "Order Updates"

    /** Create notification channel (required for Android 8+) */
    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for order status and confirmations"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /** Show order confirmation notification with token number */
    fun showOrderConfirmation(context: Context, tokenNumber: Int, totalPrice: Double) {
        // Check permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                return // Permission not granted, skip notification
            }
        }


        // Notifications banaya build kiya customised
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_check_circle)
            .setContentTitle("Order Confirmed! 🎉")
            .setContentText("Token #$tokenNumber — Total: ₹${totalPrice.toInt()}")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Your order has been placed successfully!\nToken Number: #$tokenNumber\nTotal Amount: ₹${totalPrice.toInt()}\n\nPlease collect your order from the VES Canteen counter."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(tokenNumber, notification)
    }

    /** Show a simple notification */
    fun showNotification(context: Context, title: String, message: String, id: Int = 1) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_food)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(id, notification)
    }
}
