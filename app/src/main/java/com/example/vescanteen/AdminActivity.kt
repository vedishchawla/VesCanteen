package com.example.vescanteen

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vescanteen.adapter.AdminMenuAdapter
import com.example.vescanteen.adapter.AdminOrderAdapter
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * Admin Activity — Manage menu items and view orders.
 * Only accessible when logged in as admin@vescanteen.com
 */
class AdminActivity : AppCompatActivity() {

    private lateinit var btnTabMenu: MaterialButton
    private lateinit var btnTabOrders: MaterialButton
    private lateinit var adminContent: android.widget.FrameLayout
    private lateinit var fabAddItem: FloatingActionButton
    private lateinit var btnAdminLogout: MaterialButton

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var currentTab = "menu"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        btnTabMenu = findViewById(R.id.btnTabMenu)
        btnTabOrders = findViewById(R.id.btnTabOrders)
        adminContent = findViewById(R.id.adminContent)
        fabAddItem = findViewById(R.id.fabAddItem)
        btnAdminLogout = findViewById(R.id.btnAdminLogout)

        // Tab switching
        btnTabMenu.setOnClickListener { showMenuTab() }
        btnTabOrders.setOnClickListener { showOrdersTab() }

        // FAB — Add new menu item
        fabAddItem.setOnClickListener { showAddItemDialog() }

        // Logout
        btnAdminLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Default tab
        showMenuTab()
    }

    private fun showMenuTab() {
        currentTab = "menu"
        fabAddItem.visibility = View.VISIBLE
        btnTabMenu.setTextColor(getColor(R.color.primary))
        btnTabOrders.setTextColor(getColor(R.color.gray_medium))

        // Inflate menu fragment layout
        val view = LayoutInflater.from(this).inflate(R.layout.fragment_admin_menu, adminContent, false)
        adminContent.removeAllViews()
        adminContent.addView(view)

        val rv = view.findViewById<RecyclerView>(R.id.rvAdminMenu)
        rv.layoutManager = LinearLayoutManager(this)

        // Load menu items from Firestore
        db.collection("menuItems").get()
            .addOnSuccessListener { result ->
                val items = result.map { doc ->
                    Triple(doc.id, doc.getString("name") ?: "", "${doc.getString("category") ?: ""} • ₹${doc.getDouble("price")?.toInt() ?: 0}")
                }
                rv.adapter = AdminMenuAdapter(items) { docId ->
                    // Delete item
                    db.collection("menuItems").document(docId).delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show()
                            showMenuTab() // Refresh
                        }
                }
            }
    }

    private fun showOrdersTab() {
        currentTab = "orders"
        fabAddItem.visibility = View.GONE
        btnTabMenu.setTextColor(getColor(R.color.gray_medium))
        btnTabOrders.setTextColor(getColor(R.color.primary))

        val view = LayoutInflater.from(this).inflate(R.layout.fragment_admin_orders, adminContent, false)
        adminContent.removeAllViews()
        adminContent.addView(view)

        val rv = view.findViewById<RecyclerView>(R.id.rvAdminOrders)
        rv.layoutManager = LinearLayoutManager(this)

        // Load orders from Firestore (newest first)
        db.collection("orders")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val orders = result.map { doc ->
                    val token = doc.getLong("tokenNumber")?.toInt() ?: 0
                    val total = doc.getDouble("totalPrice") ?: 0.0
                    val status = doc.getString("status") ?: "confirmed"
                    val timestamp = doc.getLong("timestamp") ?: 0
                    val items = doc.get("items") as? List<Map<String, Any>> ?: emptyList()
                    val itemNames = items.joinToString(", ") {
                        "${it["name"] ?: "?"} ×${(it["quantity"] as? Number)?.toInt() ?: 1}"
                    }
                    mapOf(
                        "token" to token,
                        "total" to total,
                        "status" to status,
                        "timestamp" to timestamp,
                        "items" to itemNames
                    )
                }
                rv.adapter = AdminOrderAdapter(orders)
            }
    }

    private fun showAddItemDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val etName = dialogView.findViewById<TextInputEditText>(R.id.etItemName)
        val etPrice = dialogView.findViewById<TextInputEditText>(R.id.etItemPrice)
        val etCategory = dialogView.findViewById<TextInputEditText>(R.id.etItemCategory)
        val etImageUrl = dialogView.findViewById<TextInputEditText>(R.id.etItemImageUrl)
        val btnAdd = dialogView.findViewById<MaterialButton>(R.id.btnAddItem)

        btnAdd.setOnClickListener {
            val name = etName.text.toString().trim()
            val priceStr = etPrice.text.toString().trim()
            val category = etCategory.text.toString().trim()
            val imageUrl = etImageUrl.text.toString().trim()

            if (name.isEmpty() || priceStr.isEmpty() || category.isEmpty()) {
                Toast.makeText(this, "Fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val item = hashMapOf(
                "name" to name,
                "price" to (priceStr.toDoubleOrNull() ?: 0.0),
                "category" to category,
                "imageUrl" to imageUrl,
                "description" to "",
                "isAvailable" to true
            )

            db.collection("menuItems").add(item)
                .addOnSuccessListener {
                    Toast.makeText(this, "$name added!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    showMenuTab() // Refresh
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        dialog.show()
    }
}
