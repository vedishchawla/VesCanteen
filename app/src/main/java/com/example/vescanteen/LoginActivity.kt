package com.example.vescanteen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
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
 *
 * Exp 5: Uses SharedPreferences to remember user's email ("Remember Me").
 */
class LoginActivity : AppCompatActivity() {

    companion object {
        const val ADMIN_EMAIL = "admin@vescanteen.com"
        const val ADMIN_PASSWORD = "admin123"
        private const val PREF_NAME = "ves_canteen_prefs"
        private const val KEY_REMEMBER_EMAIL = "remember_email"
        private const val KEY_SAVED_EMAIL = "saved_email"
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvSignup: TextView
    private lateinit var cbRememberMe: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        progressBar = findViewById(R.id.progressBar)
        tvSignup = findViewById(R.id.tvSignup)
        cbRememberMe = findViewById(R.id.cbRememberMe)

        // Exp 5: Load saved email from SharedPreferences
        loadSavedEmail()

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

    /** Exp 5: Load saved email from SharedPreferences */
    private fun loadSavedEmail() {
        val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val rememberMe = prefs.getBoolean(KEY_REMEMBER_EMAIL, false)
        if (rememberMe) {
            val savedEmail = prefs.getString(KEY_SAVED_EMAIL, "") ?: ""
            etEmail.setText(savedEmail)
            cbRememberMe.isChecked = true
        }
    }

    /** Exp 5: Save email to SharedPreferences */
    private fun saveEmailPreference(email: String) {
        val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean(KEY_REMEMBER_EMAIL, cbRememberMe.isChecked)
            if (cbRememberMe.isChecked) {
                putString(KEY_SAVED_EMAIL, email)
            } else {
                remove(KEY_SAVED_EMAIL)
            }
            apply()
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

        // Exp 5: Save email preference before login attempt
        saveEmailPreference(email)

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
