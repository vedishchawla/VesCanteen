package com.example.vescanteen

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vescanteen.adapter.AdminMenuAdapter
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Admin Menu Fragment — CRUD operations on menu items.
 */
class AdminMenuFragment : Fragment() {

    companion object {
        private const val TAG = "AdminMenu"
    }

    private val db = FirebaseFirestore.getInstance()

    private lateinit var rvMenu: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var fabAdd: FloatingActionButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_admin_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvMenu = view.findViewById(R.id.rvAdminMenu)
        emptyState = view.findViewById(R.id.emptyMenuState)
        progressBar = view.findViewById(R.id.menuProgress)
        fabAdd = view.findViewById(R.id.fabAddItem)

        rvMenu.layoutManager = LinearLayoutManager(context)
        fabAdd.setOnClickListener { showAddOrResetDialog() }

        loadMenuItems()
    }

    private fun loadMenuItems() {
        progressBar.visibility = View.VISIBLE

        db.collection("menuItems").get()
            .addOnSuccessListener { result ->
                progressBar.visibility = View.GONE
                Log.d(TAG, "Loaded ${result.size()} menu items")

                if (result.isEmpty) {
                    rvMenu.visibility = View.GONE
                    emptyState.visibility = View.VISIBLE
                } else {
                    rvMenu.visibility = View.VISIBLE
                    emptyState.visibility = View.GONE

                    val items = result.map { doc ->
                        Triple(
                            doc.id,
                            doc.getString("name") ?: "",
                            "${doc.getString("category") ?: ""} • ₹${doc.getDouble("price")?.toInt() ?: 0}"
                        )
                    }
                    rvMenu.adapter = AdminMenuAdapter(items) { docId ->
                        confirmDelete(docId)
                    }
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                rvMenu.visibility = View.GONE
                emptyState.visibility = View.VISIBLE
                Log.e(TAG, "Load failed: ${e.message}")
                Toast.makeText(context, "Error loading menu: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun confirmDelete(docId: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Item")
            .setMessage("Remove this item from the menu?")
            .setPositiveButton("Delete") { _, _ ->
                db.collection("menuItems").document(docId).delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                        loadMenuItems()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Delete failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddOrResetDialog() {
        val options = arrayOf("➕ Add New Item", "🔄 Reset Menu (Load 10 Defaults)")
        AlertDialog.Builder(requireContext())
            .setTitle("Menu Actions")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showAddItemDialog()
                    1 -> confirmResetMenu()
                }
            }
            .show()
    }

    private fun confirmResetMenu() {
        AlertDialog.Builder(requireContext())
            .setTitle("Reset Menu")
            .setMessage("This will delete ALL current items and load 10 default items. Continue?")
            .setPositiveButton("Reset") { _, _ -> resetMenuWithDefaults() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /** Deletes all items, then adds 10 defaults one by one */
    private fun resetMenuWithDefaults() {
        progressBar.visibility = View.VISIBLE
        Toast.makeText(context, "Resetting menu...", Toast.LENGTH_SHORT).show()

        // Step 1: Delete all existing
        db.collection("menuItems").get()
            .addOnSuccessListener { result ->
                Log.d(TAG, "Deleting ${result.size()} existing items")

                if (result.isEmpty) {
                    // Nothing to delete, just seed
                    seedDefaults()
                    return@addOnSuccessListener
                }

                var deleteCount = 0
                val total = result.size()

                for (doc in result) {
                    doc.reference.delete()
                        .addOnSuccessListener {
                            deleteCount++
                            Log.d(TAG, "Deleted $deleteCount/$total")
                            if (deleteCount >= total) {
                                // All deleted, now seed
                                seedDefaults()
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Delete failed: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(context, "Reset failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    /** Adds 10 default items to Firestore one by one */
    private fun seedDefaults() {
        val defaults = HomeFragment.getDefaultMenuItems()
        var addedCount = 0

        for (item in defaults) {
            val data = hashMapOf(
                "name" to item.name,
                "price" to item.price,
                "category" to item.category,
                "imageUrl" to "",
                "description" to item.description,
                "isAvailable" to true,
                "drawableResName" to item.drawableResName
            )

            db.collection("menuItems").add(data)
                .addOnSuccessListener {
                    addedCount++
                    Log.d(TAG, "Added ${item.name} ($addedCount/${defaults.size})")

                    if (addedCount >= defaults.size) {
                        progressBar.visibility = View.GONE
                        Toast.makeText(context, "✅ 10 menu items loaded!", Toast.LENGTH_SHORT).show()
                        loadMenuItems()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to add ${item.name}: ${e.message}")
                    Toast.makeText(context, "Failed to add ${item.name}: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun showAddItemDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_item, null)
        val dialog = AlertDialog.Builder(requireContext())
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
                Toast.makeText(context, "Fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val item = hashMapOf(
                "name" to name,
                "price" to (priceStr.toDoubleOrNull() ?: 0.0),
                "category" to category,
                "imageUrl" to imageUrl,
                "description" to "",
                "isAvailable" to true,
                "drawableResName" to ""
            )

            db.collection("menuItems").add(item)
                .addOnSuccessListener {
                    Toast.makeText(context, "$name added!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    loadMenuItems()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        dialog.show()
    }
}
