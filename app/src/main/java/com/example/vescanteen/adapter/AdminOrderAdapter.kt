package com.example.vescanteen.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vescanteen.R
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter for admin orders with status workflow buttons.
 * Status flow: confirmed → preparing → ready → completed
 */
class AdminOrderAdapter(
    private val orders: List<Map<String, Any>>,
    private val onStatusChange: (String, String) -> Unit
) : RecyclerView.Adapter<AdminOrderAdapter.ViewHolder>() {

    companion object {
        // Status colors
        val STATUS_COLORS = mapOf(
            "confirmed" to 0xFFFF6F00.toInt(),  // Orange
            "pending" to 0xFFFF6F00.toInt(),
            "preparing" to 0xFF1565C0.toInt(),   // Blue
            "ready" to 0xFF4CAF50.toInt(),        // Green
            "completed" to 0xFF757575.toInt()      // Gray
        )
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvToken: TextView = view.findViewById(R.id.tvOrderToken)
        val tvStatus: TextView = view.findViewById(R.id.tvOrderStatus)
        val tvItems: TextView = view.findViewById(R.id.tvOrderItems)
        val tvPayment: TextView = view.findViewById(R.id.tvOrderPayment)
        val tvTotal: TextView = view.findViewById(R.id.tvOrderTotal)
        val tvTime: TextView = view.findViewById(R.id.tvOrderTime)
        val btnAction: MaterialButton = view.findViewById(R.id.btnOrderAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_order, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orders[position]

        val docId = order["docId"] as? String ?: ""
        val token = order["token"] as? Int ?: 0
        val total = order["total"] as? Double ?: 0.0
        val status = order["status"] as? String ?: "confirmed"
        val paymentMethod = order["paymentMethod"] as? String ?: "N/A"
        val timestamp = order["timestamp"] as? Long ?: 0
        val items = order["items"] as? String ?: ""

        holder.tvToken.text = "Token #$token"
        holder.tvItems.text = items
        holder.tvPayment.text = "💳 $paymentMethod"
        holder.tvTotal.text = "₹${total.toInt()}"

        // Format timestamp
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        holder.tvTime.text = sdf.format(Date(timestamp))

        // Status badge
        val statusLabel = when (status) {
            "confirmed", "pending" -> "Pending"
            "preparing" -> "Preparing"
            "ready" -> "Ready"
            "completed" -> "Completed"
            else -> status.replaceFirstChar { it.uppercase() }
        }
        holder.tvStatus.text = statusLabel

        // Set badge color
        val badgeColor = STATUS_COLORS[status] ?: 0xFF757575.toInt()
        val bg = holder.tvStatus.background
        if (bg is GradientDrawable) {
            bg.setColor(badgeColor)
        } else {
            val drawable = GradientDrawable().apply {
                setColor(badgeColor)
                cornerRadius = 24f
            }
            holder.tvStatus.background = drawable
        }

        // Action button based on current status
        when (status) {
            "confirmed", "pending" -> {
                holder.btnAction.visibility = View.VISIBLE
                holder.btnAction.text = "🔥 Mark as Preparing"
                holder.btnAction.setBackgroundColor(0xFF1565C0.toInt())
                holder.btnAction.setOnClickListener {
                    onStatusChange(docId, "preparing")
                }
            }
            "preparing" -> {
                holder.btnAction.visibility = View.VISIBLE
                holder.btnAction.text = "✅ Mark as Ready"
                holder.btnAction.setBackgroundColor(0xFF4CAF50.toInt())
                holder.btnAction.setOnClickListener {
                    onStatusChange(docId, "ready")
                }
            }
            "ready" -> {
                holder.btnAction.visibility = View.VISIBLE
                holder.btnAction.text = "📦 Mark as Completed"
                holder.btnAction.setBackgroundColor(0xFF757575.toInt())
                holder.btnAction.setOnClickListener {
                    onStatusChange(docId, "completed")
                }
            }
            "completed" -> {
                holder.btnAction.visibility = View.GONE
            }
        }
    }

    override fun getItemCount() = orders.size
}
