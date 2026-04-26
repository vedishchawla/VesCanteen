package com.example.vescanteen

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Background Service — Periodically checks order status from Firestore.
 * Exp A2: Background tasks using Android Services.
 *
 * How it works:
 * - Started after placing an order
 * - Polls Firestore every 30 seconds for status changes
 * - When status changes to "ready", shows a notification
 * - Auto-stops when order is "completed" or "ready"
 */
class OrderStatusService : Service() {

    companion object {
        const val EXTRA_ORDER_DOC_ID = "order_doc_id"
        const val EXTRA_TOKEN_NUMBER = "token_number"
        private const val TAG = "OrderStatusService"
        private const val POLL_INTERVAL_MS = 30_000L  // 30 seconds
    }

    private val db = FirebaseFirestore.getInstance()
    private val handler = Handler(Looper.getMainLooper())
    private var orderDocId: String? = null
    private var tokenNumber: Int = 0
    private var lastKnownStatus = "confirmed"
    private var isRunning = false

    override fun onBind(intent: Intent?): IBinder? = null  // Not a bound service

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        orderDocId = intent?.getStringExtra(EXTRA_ORDER_DOC_ID)
        tokenNumber = intent?.getIntExtra(EXTRA_TOKEN_NUMBER, 0) ?: 0

        Log.d(TAG, "Service started for order: $orderDocId, token: $tokenNumber")

        if (orderDocId != null && !isRunning) {
            isRunning = true
            NotificationHelper.createChannel(this)
            startPolling()
        }

        return START_STICKY  // Restart if killed by system
    }

    /** Poll Firestore periodically for status updates */
    private fun startPolling() {
        handler.post(object : Runnable {
            override fun run() {
                if (!isRunning) return

                checkOrderStatus()

                // Schedule next check
                handler.postDelayed(this, POLL_INTERVAL_MS)
            }
        })
    }

    /** Check current order status from Firestore */
    private fun checkOrderStatus() {
        val docId = orderDocId ?: return

        db.collection("orders").document(docId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val newStatus = doc.getString("status") ?: "confirmed"
                    Log.d(TAG, "Order $docId status: $newStatus (was: $lastKnownStatus)")

                    // Status changed!
                    if (newStatus != lastKnownStatus) {
                        when (newStatus) {
                            "preparing" -> {
                                NotificationHelper.showNotification(
                                    this,
                                    "Order Being Prepared 🍳",
                                    "Your order #$tokenNumber is being prepared!",
                                    tokenNumber + 1000
                                )
                            }
                            "ready" -> {
                                NotificationHelper.showNotification(
                                    this,
                                    "Order Ready! 🎉",
                                    "Order #$tokenNumber is ready! Collect from the counter.",
                                    tokenNumber + 2000
                                )
                                // Stop service — order is ready
                                stopSelfAndCleanup()
                            }
                            "completed" -> {
                                stopSelfAndCleanup()
                            }
                        }
                        lastKnownStatus = newStatus
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to check status: ${e.message}")
            }
    }

    private fun stopSelfAndCleanup() {
        isRunning = false
        handler.removeCallbacksAndMessages(null)
        stopSelf()
        Log.d(TAG, "Service stopped for order $orderDocId")
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        handler.removeCallbacksAndMessages(null)
        Log.d(TAG, "Service destroyed")
    }
}
