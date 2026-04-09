package com.example.vescanteen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vescanteen.adapter.AdminOrderAdapter
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

/**
 * Admin Orders Fragment — View and manage all orders with status workflow.
 * Status flow: confirmed → preparing → ready → completed
 */
class AdminOrdersFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private var ordersListener: ListenerRegistration? = null
    private var currentFilter = "all"

    private lateinit var rvOrders: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var tvEmptyMsg: TextView
    private lateinit var progressBar: ProgressBar

    private var allOrders = mutableListOf<Map<String, Any>>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_admin_orders, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvOrders = view.findViewById(R.id.rvAdminOrders)
        emptyState = view.findViewById(R.id.emptyOrderState)
        tvEmptyMsg = view.findViewById(R.id.tvEmptyOrderMsg)
        progressBar = view.findViewById(R.id.ordersProgress)

        val chipAll = view.findViewById<Chip>(R.id.chipAllOrders)
        val chipPending = view.findViewById<Chip>(R.id.chipPending)
        val chipPreparing = view.findViewById<Chip>(R.id.chipPreparing)
        val chipReady = view.findViewById<Chip>(R.id.chipReady)

        rvOrders.layoutManager = LinearLayoutManager(context)

        chipAll.setOnClickListener { currentFilter = "all"; applyFilter() }
        chipPending.setOnClickListener { currentFilter = "confirmed"; applyFilter() }
        chipPreparing.setOnClickListener { currentFilter = "preparing"; applyFilter() }
        chipReady.setOnClickListener { currentFilter = "ready"; applyFilter() }

        loadOrders()
    }

    private fun loadOrders() {
        progressBar.visibility = View.VISIBLE

        ordersListener = db.collection("orders")
            .addSnapshotListener { snapshot, error ->
                progressBar.visibility = View.GONE

                if (error != null || snapshot == null) return@addSnapshotListener

                allOrders.clear()
                for (doc in snapshot.documents) {
                    val items = doc.get("items") as? List<Map<String, Any>> ?: emptyList()
                    val itemNames = items.joinToString(", ") {
                        "${it["name"] ?: "?"} ×${(it["quantity"] as? Number)?.toInt() ?: 1}"
                    }
                    allOrders.add(mapOf(
                        "docId" to doc.id,
                        "token" to (doc.getLong("tokenNumber")?.toInt() ?: 0),
                        "total" to (doc.getDouble("totalPrice") ?: 0.0),
                        "status" to (doc.getString("status") ?: "confirmed"),
                        "paymentMethod" to (doc.getString("paymentMethod") ?: "N/A"),
                        "timestamp" to (doc.getLong("timestamp") ?: 0),
                        "items" to itemNames
                    ))
                }

                // Sort newest first
                allOrders.sortByDescending { it["timestamp"] as Long }
                applyFilter()
            }
    }

    private fun applyFilter() {
        val filtered = if (currentFilter == "all") {
            allOrders
        } else {
            allOrders.filter { it["status"] == currentFilter }
        }

        if (filtered.isEmpty()) {
            rvOrders.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
            tvEmptyMsg.text = if (currentFilter == "all") "No orders yet" else "No $currentFilter orders"
        } else {
            rvOrders.visibility = View.VISIBLE
            emptyState.visibility = View.GONE
            rvOrders.adapter = AdminOrderAdapter(filtered) { docId, newStatus ->
                db.collection("orders").document(docId).update("status", newStatus)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ordersListener?.remove()
    }
}
