package com.example.vescanteen

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Main Activity - Shell with BottomNavigationView.
 * Swaps between 4 fragments: Home, Search, Cart, Profile.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Exp 5: Initialize CartManager with context for SharedPreferences persistence
        CartManager.init(this)

        bottomNav = findViewById(R.id.bottomNav)

        // Load Home fragment by default
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        // Handle bottom nav item clicks
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_search -> {
                    loadFragment(SearchFragment())
                    true
                }
                R.id.nav_cart -> {
                    loadFragment(CartFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    /** Navigate to Cart tab programmatically (called from Home) */
    fun navigateToCart() {
        bottomNav.selectedItemId = R.id.nav_cart
    }
}
