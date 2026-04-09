package com.example.vescanteen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
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
 * Home Fragment - Displays menu items from Firestore in a grid.
 * If Firestore is empty, seeds default items to Firestore first.
 * This ensures admin and customer see the same menu.
 */
class HomeFragment : Fragment() {

    private lateinit var rvMenu: RecyclerView
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

        loadMenuItems()
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

        val user = auth.currentUser
        if (user != null) {
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    val username = doc.getString("username") ?: "Student"
                    tvGreeting.text = "$timeGreeting, $username! ☀️"
                }
                .addOnFailureListener {
                    tvGreeting.text = "$timeGreeting!"
                }
        } else {
            tvGreeting.text = "$timeGreeting!"
        }
    }

    /** Load menu from Firestore. If empty, seed defaults TO Firestore first. */
    private fun loadMenuItems() {
        db.collection("menuItems").get()
            .addOnSuccessListener { result ->
                allItems.clear()

                if (result.isEmpty) {
                    // No items in Firestore — seed defaults
                    seedDefaultItemsToFirestore()
                } else {
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
            .addOnFailureListener {
                // Offline fallback — load hardcoded
                loadHardcodedDefaults()
                filterByCategory(currentCategory)
            }
    }

    /** Seeds the 10 default items to Firestore, then reloads */
    private fun seedDefaultItemsToFirestore() {
        val defaults = listOf(
            mapOf("name" to "Poha", "price" to 35.0, "category" to "Breakfast", "imageUrl" to "", "description" to "Light and healthy flattened rice", "isAvailable" to true, "drawableResName" to "food_poha"),
            mapOf("name" to "Chai", "price" to 10.0, "category" to "Beverages", "imageUrl" to "", "description" to "Hot Indian tea", "isAvailable" to true, "drawableResName" to "food_chai"),
            mapOf("name" to "Samosa", "price" to 10.0, "category" to "Breakfast", "imageUrl" to "", "description" to "Crispy fried pastry", "isAvailable" to true, "drawableResName" to "food_samosa"),
            mapOf("name" to "Vada Pav", "price" to 15.0, "category" to "Breakfast", "imageUrl" to "", "description" to "Mumbai's favourite snack", "isAvailable" to true, "drawableResName" to "food_vadapav"),
            mapOf("name" to "Coffee", "price" to 15.0, "category" to "Beverages", "imageUrl" to "", "description" to "Fresh brewed coffee", "isAvailable" to true, "drawableResName" to "food_coffee"),
            mapOf("name" to "Sandwich", "price" to 30.0, "category" to "Breakfast", "imageUrl" to "", "description" to "Grilled veg sandwich", "isAvailable" to true, "drawableResName" to "food_sandwich"),
            mapOf("name" to "Juice", "price" to 25.0, "category" to "Beverages", "imageUrl" to "", "description" to "Fresh fruit juice", "isAvailable" to true, "drawableResName" to "food_juice"),
            mapOf("name" to "Maggi", "price" to 25.0, "category" to "For You", "imageUrl" to "", "description" to "2-minute noodles", "isAvailable" to true, "drawableResName" to "food_maggi"),
            mapOf("name" to "Dosa", "price" to 40.0, "category" to "Breakfast", "imageUrl" to "", "description" to "Crispy South Indian crepe", "isAvailable" to true, "drawableResName" to "food_dosa"),
            mapOf("name" to "Lassi", "price" to 20.0, "category" to "Beverages", "imageUrl" to "", "description" to "Sweet yogurt drink", "isAvailable" to true, "drawableResName" to "food_lassi")
        )

        val batch = db.batch()
        for (item in defaults) {
            val docRef = db.collection("menuItems").document()
            batch.set(docRef, item)
        }
        batch.commit()
            .addOnSuccessListener {
                // Reload from Firestore
                loadMenuItems()
            }
            .addOnFailureListener {
                // Fallback
                loadHardcodedDefaults()
                filterByCategory(currentCategory)
            }
    }

    /** Offline fallback only */
    private fun loadHardcodedDefaults() {
        allItems.clear()
        allItems.addAll(listOf(
            MenuItem("1", "Poha", 35.0, "Breakfast", "", "Light and healthy flattened rice", true, "food_poha"),
            MenuItem("2", "Chai", 10.0, "Beverages", "", "Hot Indian tea", true, "food_chai"),
            MenuItem("3", "Samosa", 10.0, "Breakfast", "", "Crispy fried pastry", true, "food_samosa"),
            MenuItem("4", "Vada Pav", 15.0, "Breakfast", "", "Mumbai's favourite snack", true, "food_vadapav"),
            MenuItem("5", "Coffee", 15.0, "Beverages", "", "Fresh brewed coffee", true, "food_coffee"),
            MenuItem("6", "Sandwich", 30.0, "Breakfast", "", "Grilled veg sandwich", true, "food_sandwich"),
            MenuItem("7", "Juice", 25.0, "Beverages", "", "Fresh fruit juice", true, "food_juice"),
            MenuItem("8", "Maggi", 25.0, "For You", "", "2-minute noodles", true, "food_maggi"),
            MenuItem("9", "Dosa", 40.0, "Breakfast", "", "Crispy South Indian crepe", true, "food_dosa"),
            MenuItem("10", "Lassi", 20.0, "Beverages", "", "Sweet yogurt drink", true, "food_lassi")
        ))
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
