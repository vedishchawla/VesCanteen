package com.example.vescanteen.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vescanteen.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter for admin orders view.
 * Shows token, status, items, total, and time.
 */
class AdminOrderAdapter(
    private val orders: List<Map<String, Any>>
) : RecyclerView.Adapter<AdminOrderAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvToken: TextView = view.findViewById(R.id.tvOrderToken)
        val tvStatus: TextView = view.findViewById(R.id.tvOrderStatus)
        val tvItems: TextView = view.findViewById(R.id.tvOrderItems)
        val tvTotal: TextView = view.findViewById(R.id.tvOrderTotal)
        val tvTime: TextView = view.findViewById(R.id.tvOrderTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_order, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orders[position]

        val token = order["token"] as? Int ?: 0
        val total = order["total"] as? Double ?: 0.0
        val status = order["status"] as? String ?: "confirmed"
        val paymentMethod = order["paymentMethod"] as? String ?: "N/A"
        val timestamp = order["timestamp"] as? Long ?: 0
        val items = order["items"] as? String ?: ""

        holder.tvToken.text = "Token #$token"
        holder.tvStatus.text = status.replaceFirstChar { it.uppercase() }
        holder.tvItems.text = "$items\nPayment: $paymentMethod"
        holder.tvTotal.text = "₹${total.toInt()}"

        // Format timestamp
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        holder.tvTime.text = sdf.format(Date(timestamp))
    }

    override fun getItemCount() = orders.size
}
