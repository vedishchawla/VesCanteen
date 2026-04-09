package com.example.vescanteen

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

/**
 * Admin Activity — Shell with bottom nav (Dashboard / Orders / Menu).
 */
class AdminActivity : AppCompatActivity() {

    private lateinit var adminBottomNav: BottomNavigationView
    private lateinit var tvAdminTitle: TextView
    private lateinit var btnAdminLogout: MaterialButton

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        adminBottomNav = findViewById(R.id.adminBottomNav)
        tvAdminTitle = findViewById(R.id.tvAdminTitle)
        btnAdminLogout = findViewById(R.id.btnAdminLogout)

        // Logout
        btnAdminLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Default — Dashboard
        if (savedInstanceState == null) {
            loadFragment(AdminDashboardFragment())
            tvAdminTitle.text = "Dashboard"
        }

        // Bottom nav
        adminBottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    tvAdminTitle.text = "Dashboard"
                    loadFragment(AdminDashboardFragment())
                    true
                }
                R.id.nav_orders -> {
                    tvAdminTitle.text = "Orders"
                    loadFragment(AdminOrdersFragment())
                    true
                }
                R.id.nav_menu_mgmt -> {
                    tvAdminTitle.text = "Menu"
                    loadFragment(AdminMenuFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.adminFragmentContainer, fragment)
            .commit()
    }
}
