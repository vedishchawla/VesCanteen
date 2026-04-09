package com.example.vescanteen

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Profile Fragment - Shows user info and logout button.
 */
class ProfileFragment : Fragment() {

    private lateinit var tvAvatar: TextView
    private lateinit var tvProfileName: TextView
    private lateinit var tvProfileEmail: TextView
    private lateinit var tvProfilePhone: TextView
    private lateinit var tvProfileUid: TextView
    private lateinit var btnLogout: MaterialButton

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvAvatar = view.findViewById(R.id.tvAvatar)
        tvProfileName = view.findViewById(R.id.tvProfileName)
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail)
        tvProfilePhone = view.findViewById(R.id.tvProfilePhone)
        tvProfileUid = view.findViewById(R.id.tvProfileUid)
        btnLogout = view.findViewById(R.id.btnLogout)

        // Load user data
        loadUserProfile()

        // Logout button
        btnLogout.setOnClickListener {
            auth.signOut()
            CartManager.clearCart()
            Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
            startActivity(Intent(context, LoginActivity::class.java))
            activity?.finish()
        }
    }

    private fun loadUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            tvProfileEmail.text = user.email ?: "No email"
            tvProfileUid.text = user.uid

            // Get avatar initial from email
            val initial = (user.email?.firstOrNull() ?: 'U').uppercaseChar()
            tvAvatar.text = initial.toString()

            // Load full profile from Firestore
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        tvProfileName.text = doc.getString("username") ?: "Student"
                        tvProfilePhone.text = doc.getString("phone") ?: "Not set"

                        // Update avatar with username initial
                        val name = doc.getString("username") ?: ""
                        if (name.isNotEmpty()) {
                            tvAvatar.text = name.first().uppercaseChar().toString()
                        }
                    }
                }
        }
    }
}
