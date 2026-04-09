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

/**
 * Login Activity — Firebase email/password auth.
 * Only allows @ves.ac.in emails (students) and admin@vescanteen.com.
 * Auto-creates admin account on first login attempt.
 */
class LoginActivity : AppCompatActivity() {

    companion object {
        const val ADMIN_EMAIL = "admin@vescanteen.com"
        const val ADMIN_PASSWORD = "admin123"
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvSignup: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        progressBar = findViewById(R.id.progressBar)
        tvSignup = findViewById(R.id.tvSignup)

        btnLogin.setOnClickListener { loginUser() }

        tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            navigateBasedOnRole(currentUser.email ?: "")
        }
    }

    private fun loginUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            etEmail.requestFocus()
            return
        }
        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            etPassword.requestFocus()
            return
        }

        // Validate email domain: only @ves.ac.in or admin
        if (!isValidEmail(email)) {
            etEmail.error = "Only @ves.ac.in emails are allowed"
            etEmail.requestFocus()
            return
        }

        progressBar.visibility = View.VISIBLE
        btnLogin.isEnabled = false

        // If admin, try login — if fails, auto-register then login
        if (email.equals(ADMIN_EMAIL, ignoreCase = true)) {
            loginAdmin(email, password)
        } else {
            loginRegular(email, password)
        }
    }

    private fun loginRegular(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                progressBar.visibility = View.GONE
                btnLogin.isEnabled = true

                if (task.isSuccessful) {
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    navigateBasedOnRole(email)
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG).show()
                }
            }
    }

    /** Admin login — auto-creates account if it doesn't exist */
    private fun loginAdmin(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    progressBar.visibility = View.GONE
                    btnLogin.isEnabled = true
                    Toast.makeText(this, "Admin login successful!", Toast.LENGTH_SHORT).show()
                    navigateBasedOnRole(email)
                } else {
                    // Account doesn't exist — create it automatically
                    auth.createUserWithEmailAndPassword(ADMIN_EMAIL, ADMIN_PASSWORD)
                        .addOnCompleteListener(this) { regTask ->
                            progressBar.visibility = View.GONE
                            btnLogin.isEnabled = true

                            if (regTask.isSuccessful) {
                                Toast.makeText(this, "Admin account created & logged in!", Toast.LENGTH_SHORT).show()
                                navigateBasedOnRole(ADMIN_EMAIL)
                            } else {
                                Toast.makeText(this, "Admin login failed: ${regTask.exception?.message}",
                                    Toast.LENGTH_LONG).show()
                            }
                        }
                }
            }
    }

    /** Check if email is either admin or @ves.ac.in */
    private fun isValidEmail(email: String): Boolean {
        return email.equals(ADMIN_EMAIL, ignoreCase = true) ||
                email.lowercase().endsWith("@ves.ac.in")
    }

    /** Admin goes to AdminActivity, everyone else to MainActivity */
    private fun navigateBasedOnRole(email: String) {
        if (email.equals(ADMIN_EMAIL, ignoreCase = true)) {
            startActivity(Intent(this, AdminActivity::class.java))
        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }
}
