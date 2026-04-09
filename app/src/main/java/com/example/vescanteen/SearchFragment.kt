package com.example.vescanteen

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vescanteen.adapter.MenuAdapter
import com.example.vescanteen.model.MenuItem
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Search Fragment - Search for food items by name.
 */
class SearchFragment : Fragment() {

    private lateinit var etSearch: EditText
    private lateinit var rvSearchResults: RecyclerView
    private lateinit var searchEmptyState: LinearLayout
    private lateinit var tvSearchHint: TextView
    private lateinit var menuAdapter: MenuAdapter

    private val allItems = mutableListOf<MenuItem>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etSearch = view.findViewById(R.id.etSearch)
        rvSearchResults = view.findViewById(R.id.rvSearchResults)
        searchEmptyState = view.findViewById(R.id.searchEmptyState)
        tvSearchHint = view.findViewById(R.id.tvSearchHint)

        menuAdapter = MenuAdapter(emptyList()) {
            // Cart changed — no additional action needed in search
        }
        rvSearchResults.layoutManager = GridLayoutManager(context, 2)
        rvSearchResults.adapter = menuAdapter

        loadAllItems()

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim().lowercase()
                if (query.isEmpty()) {
                    rvSearchResults.visibility = View.GONE
                    searchEmptyState.visibility = View.VISIBLE
                    tvSearchHint.text = "Search for your favourite food"
                } else {
                    val filtered = allItems.filter { it.name.lowercase().contains(query) }
                    if (filtered.isEmpty()) {
                        rvSearchResults.visibility = View.GONE
                        searchEmptyState.visibility = View.VISIBLE
                        tvSearchHint.text = "No results found for \"$query\""
                    } else {
                        rvSearchResults.visibility = View.VISIBLE
                        searchEmptyState.visibility = View.GONE
                        menuAdapter.updateItems(filtered)
                    }
                }
            }
        })
    }

    private fun loadAllItems() {
        db.collection("menuItems").get()
            .addOnSuccessListener { result ->
                allItems.clear()
                for (doc in result) {
                    val item = doc.toObject(MenuItem::class.java).copy(id = doc.id)
                    allItems.add(item)
                }
                if (allItems.isEmpty()) loadDefaults()
            }
            .addOnFailureListener { loadDefaults() }
    }

    private fun loadDefaults() {
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
}
