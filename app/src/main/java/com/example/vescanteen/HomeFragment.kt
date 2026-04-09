package com.example.vescanteen

import android.os.Bundle
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

/**
 * Home Fragment - Displays menu items in a grid.
 * Fetches items from Firestore and shows by category.
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

        loadUsername()

        menuAdapter = MenuAdapter(emptyList()) { menuItem ->
            CartManager.addItem(menuItem)
            updateCartBadge()
            Toast.makeText(context, "${menuItem.name} added to cart!", Toast.LENGTH_SHORT).show()
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
    }

    private fun loadUsername() {
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
        }
    }

    private fun loadMenuItems() {
        db.collection("menuItems").get()
            .addOnSuccessListener { result ->
                allItems.clear()
                for (doc in result) {
                    val item = doc.toObject(MenuItem::class.java).copy(id = doc.id)
                    allItems.add(item)
                }
                if (allItems.isEmpty()) {
                    loadDefaultItems()
                }
                filterByCategory(currentCategory)
            }
            .addOnFailureListener {
                loadDefaultItems()
                filterByCategory(currentCategory)
            }
    }

    private fun loadDefaultItems() {
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
