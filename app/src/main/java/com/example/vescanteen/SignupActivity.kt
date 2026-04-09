package com.example.vescanteen

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Signup Activity - Creates new user accounts with Firebase Auth.
 * Also saves user profile (username, email, phone) to Firestore.
 */
class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var etUsername: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnSignup: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize UI
        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnSignup = findViewById(R.id.btnSignup)
        progressBar = findViewById(R.id.progressBar)
        tvLogin = findViewById(R.id.tvLogin)

        // Signup button click
        btnSignup.setOnClickListener {
            signupUser()
        }

        // Navigate back to Login screen
        tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun signupUser() {
        val username = etUsername.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        // Validate input
        if (username.isEmpty()) {
            etUsername.error = "Username is required"
            etUsername.requestFocus()
            return
        }
        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            etEmail.requestFocus()
            return
        }
        if (phone.isEmpty()) {
            etPhone.error = "Phone number is required"
            etPhone.requestFocus()
            return
        }
        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            etPassword.requestFocus()
            return
        }
        if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            etPassword.requestFocus()
            return
        }
        if (password != confirmPassword) {
            etConfirmPassword.error = "Passwords do not match"
            etConfirmPassword.requestFocus()
            return
        }

        // Show loading
        progressBar.visibility = View.VISIBLE
        btnSignup.isEnabled = false

        // Create user with Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Save user profile to Firestore
                    val user = auth.currentUser
                    val userProfile = hashMapOf(
                        "uid" to (user?.uid ?: ""),
                        "username" to username,
                        "email" to email,
                        "phone" to phone
                    )

                    db.collection("users")
                        .document(user?.uid ?: "")
                        .set(userProfile)
                        .addOnSuccessListener {
                            progressBar.visibility = View.GONE
                            btnSignup.isEnabled = true
                            Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            progressBar.visibility = View.GONE
                            btnSignup.isEnabled = true
                            Toast.makeText(this, "Profile save failed: ${e.message}",
                                Toast.LENGTH_LONG).show()
                        }
                } else {
                    progressBar.visibility = View.GONE
                    btnSignup.isEnabled = true
                    Toast.makeText(this, "Signup failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG).show()
                }
            }
    }
}
