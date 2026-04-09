package com.example.vescanteen

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vescanteen.adapter.MenuAdapter
import com.example.vescanteen.model.MenuItem
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

/**
 * Home Fragment - Displays menu items in a grid.
 * Loads from Firestore. Falls back to hardcoded defaults instantly.
 */
class HomeFragment : Fragment() {

    companion object {
        private const val TAG = "HomeFragment"

        /** The 10 default menu items — shared between customer and admin */
        fun getDefaultMenuItems(): List<MenuItem> = listOf(
            MenuItem("default_1", "Poha", 35.0, "Breakfast", "", "Light and healthy flattened rice", true, "food_poha"),
            MenuItem("default_2", "Chai", 10.0, "Beverages", "", "Hot Indian tea", true, "food_chai"),
            MenuItem("default_3", "Samosa", 10.0, "Breakfast", "", "Crispy fried pastry", true, "food_samosa"),
            MenuItem("default_4", "Vada Pav", 15.0, "Breakfast", "", "Mumbai's favourite snack", true, "food_vadapav"),
            MenuItem("default_5", "Coffee", 15.0, "Beverages", "", "Fresh brewed coffee", true, "food_coffee"),
            MenuItem("default_6", "Sandwich", 30.0, "Breakfast", "", "Grilled veg sandwich", true, "food_sandwich"),
            MenuItem("default_7", "Juice", 25.0, "Beverages", "", "Fresh fruit juice", true, "food_juice"),
            MenuItem("default_8", "Maggi", 25.0, "For You", "", "2-minute noodles", true, "food_maggi"),
            MenuItem("default_9", "Dosa", 40.0, "Breakfast", "", "Crispy South Indian crepe", true, "food_dosa"),
            MenuItem("default_10", "Lassi", 20.0, "Beverages", "", "Sweet yogurt drink", true, "food_lassi")
        )
    }

    private lateinit var rvMenu: RecyclerView
    private lateinit var tvTimeGreeting: TextView
    private lateinit var tvGreeting: TextView
    private lateinit var tvCartBadge: TextView
    private lateinit var cartBadgeContainer: FrameLayout
    private lateinit var chipAll: Chip
    private lateinit var chipBreakfast: Chip
    private lateinit var chipBeverages: Chip
    private lateinit var menuAdapter: MenuAdapter

    private val allItems = mutableListOf<MenuItem>()
    private var currentCategory = "For You"

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvMenu = view.findViewById(R.id.rvMenu)
        tvTimeGreeting = view.findViewById(R.id.tvTimeGreeting)
        tvGreeting = view.findViewById(R.id.tvGreeting)
        tvCartBadge = view.findViewById(R.id.tvCartBadge)
        cartBadgeContainer = view.findViewById(R.id.cartBadgeContainer)
        chipAll = view.findViewById(R.id.chipAll)
        chipBreakfast = view.findViewById(R.id.chipBreakfast)
        chipBeverages = view.findViewById(R.id.chipBeverages)

        loadGreeting()

        menuAdapter = MenuAdapter(emptyList()) {
            updateCartBadge()
        }
        rvMenu.layoutManager = GridLayoutManager(context, 2)
        rvMenu.adapter = menuAdapter

        chipAll.isChecked = true
        chipAll.setOnClickListener { filterByCategory("For You") }
        chipBreakfast.setOnClickListener { filterByCategory("Breakfast") }
        chipBeverages.setOnClickListener { filterByCategory("Beverages") }

        cartBadgeContainer.setOnClickListener {
            (activity as? MainActivity)?.navigateToCart()
        }

        // Show defaults immediately, then try Firestore
        allItems.addAll(getDefaultMenuItems())
        filterByCategory(currentCategory)
        loadMenuFromFirestore()
    }

    override fun onResume() {
        super.onResume()
        updateCartBadge()
        menuAdapter.notifyDataSetChanged()
    }

    /** Time-based greeting */
    private fun loadGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val timeGreeting = when {
            hour < 5 -> "Hey, Night Owl"
            hour < 12 -> "Good Morning"
            hour < 17 -> "Good Afternoon"
            else -> "Good Evening"
        }

        // Set the small top label
        tvTimeGreeting.text = "$timeGreeting 👋"

        val user = auth.currentUser
        if (user != null) {
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    val username = doc.getString("username") ?: "Student"
                    tvGreeting.text = "Hey, $username!"
                }
                .addOnFailureListener {
                    tvGreeting.text = "Welcome!"
                }
        } else {
            tvGreeting.text = "Welcome!"
        }
    }

    /** Try to load from Firestore. If empty, seed defaults. If fails, keep showing local defaults. */
    private fun loadMenuFromFirestore() {
        db.collection("menuItems").get()
            .addOnSuccessListener { result ->
                Log.d(TAG, "Firestore menuItems: ${result.size()} items")

                if (result.isEmpty) {
                    // Firestore empty — seed defaults to it
                    Log.d(TAG, "Firestore empty, seeding defaults...")
                    seedDefaultsToFirestore()
                } else {
                    // Load from Firestore
                    allItems.clear()
                    for (doc in result) {
                        val item = MenuItem(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            price = doc.getDouble("price") ?: 0.0,
                            category = doc.getString("category") ?: "",
                            imageUrl = doc.getString("imageUrl") ?: "",
                            description = doc.getString("description") ?: "",
                            isAvailable = doc.getBoolean("isAvailable") ?: true,
                            drawableResName = doc.getString("drawableResName") ?: ""
                        )
                        allItems.add(item)
                    }
                    filterByCategory(currentCategory)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Firestore load failed: ${e.message}")
                // Keep showing local defaults — already loaded
            }
    }

    /** Writes the 10 default items to Firestore so admin can see them */
    private fun seedDefaultsToFirestore() {
        val defaults = getDefaultMenuItems()
        var successCount = 0

        for (item in defaults) {
            val data = hashMapOf(
                "name" to item.name,
                "price" to item.price,
                "category" to item.category,
                "imageUrl" to item.imageUrl,
                "description" to item.description,
                "isAvailable" to item.isAvailable,
                "drawableResName" to item.drawableResName
            )

            db.collection("menuItems").add(data)
                .addOnSuccessListener {
                    successCount++
                    Log.d(TAG, "Seeded: ${item.name} ($successCount/10)")
                    if (successCount == defaults.size) {
                        Log.d(TAG, "All 10 items seeded to Firestore!")
                        // Reload from Firestore
                        loadMenuFromFirestore()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to seed ${item.name}: ${e.message}")
                }
        }
    }

    private fun filterByCategory(category: String) {
        currentCategory = category
        chipAll.isChecked = category == "For You"
        chipBreakfast.isChecked = category == "Breakfast"
        chipBeverages.isChecked = category == "Beverages"

        val filtered = if (category == "For You") {
            allItems
        } else {
            allItems.filter { it.category == category }
        }
        menuAdapter.updateItems(filtered)
    }

    private fun updateCartBadge() {
        val count = CartManager.getItemCount()
        if (count > 0) {
            tvCartBadge.visibility = View.VISIBLE
            tvCartBadge.text = count.toString()
        } else {
            tvCartBadge.visibility = View.GONE
        }
    }
}
