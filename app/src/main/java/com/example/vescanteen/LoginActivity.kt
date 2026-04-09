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
 * Detects admin email and redirects to AdminActivity.
 *
 * Admin Credentials:
 *   Email: admin@vescanteen.com
 *   Password: admin123
 */
class LoginActivity : AppCompatActivity() {

    companion object {
        const val ADMIN_EMAIL = "admin@vescanteen.com"
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

        progressBar.visibility = View.VISIBLE
        btnLogin.isEnabled = false

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
