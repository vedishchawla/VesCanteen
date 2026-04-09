package com.example.vescanteen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vescanteen.adapter.AdminOrderAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.Calendar

/**
 * Admin Dashboard — Shows summary stats and recent orders.
 */
class AdminDashboardFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private var ordersListener: ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvTotalOrders = view.findViewById<TextView>(R.id.tvTotalOrders)
        val tvPendingOrders = view.findViewById<TextView>(R.id.tvPendingOrders)
        val tvRevenue = view.findViewById<TextView>(R.id.tvRevenue)
        val tvMenuCount = view.findViewById<TextView>(R.id.tvMenuCount)
        val rvRecentOrders = view.findViewById<RecyclerView>(R.id.rvRecentOrders)
        val tvNoRecentOrders = view.findViewById<TextView>(R.id.tvNoRecentOrders)

        rvRecentOrders.layoutManager = LinearLayoutManager(context)

        // Get today's start timestamp
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        // Real-time orders listener
        ordersListener = db.collection("orders")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val allOrders = snapshot.documents
                val todayOrders = allOrders.filter {
                    (it.getLong("timestamp") ?: 0) >= todayStart
                }

                // Stats
                tvTotalOrders.text = todayOrders.size.toString()
                tvPendingOrders.text = todayOrders.count {
                    val status = it.getString("status") ?: ""
                    status == "confirmed" || status == "pending"
                }.toString()
                val revenue = todayOrders.sumOf { it.getDouble("totalPrice") ?: 0.0 }
                tvRevenue.text = "₹${revenue.toInt()}"

                // Recent 5 orders
                val recent = todayOrders
                    .sortedByDescending { it.getLong("timestamp") ?: 0 }
                    .take(5)
                    .map { doc ->
                        val items = doc.get("items") as? List<Map<String, Any>> ?: emptyList()
                        val itemNames = items.joinToString(", ") {
                            "${it["name"] ?: "?"} ×${(it["quantity"] as? Number)?.toInt() ?: 1}"
                        }
                        mapOf(
                            "docId" to doc.id,
                            "token" to (doc.getLong("tokenNumber")?.toInt() ?: 0),
                            "total" to (doc.getDouble("totalPrice") ?: 0.0),
                            "status" to (doc.getString("status") ?: "confirmed"),
                            "paymentMethod" to (doc.getString("paymentMethod") ?: "N/A"),
                            "timestamp" to (doc.getLong("timestamp") ?: 0),
                            "items" to itemNames
                        )
                    }

                if (recent.isEmpty()) {
                    rvRecentOrders.visibility = View.GONE
                    tvNoRecentOrders.visibility = View.VISIBLE
                } else {
                    rvRecentOrders.visibility = View.VISIBLE
                    tvNoRecentOrders.visibility = View.GONE
                    rvRecentOrders.adapter = AdminOrderAdapter(recent) { docId, newStatus ->
                        updateOrderStatus(docId, newStatus)
                    }
                }
            }

        // Menu count
        db.collection("menuItems").get().addOnSuccessListener { result ->
            tvMenuCount.text = result.size().toString()
        }
    }

    private fun updateOrderStatus(docId: String, newStatus: String) {
        db.collection("orders").document(docId).update("status", newStatus)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ordersListener?.remove()
    }
}
