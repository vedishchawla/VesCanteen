package com.example.vescanteen

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

/**
 * Payment Activity — Select payment method (UPI or Cash).
 * UPI opens the device's UPI apps (GPay / PhonePe / Paytm etc.)
 * Cash at counter skips straight to order confirmation.
 */
class PaymentActivity : AppCompatActivity() {

    companion object {
        // Replace with the canteen's actual UPI ID
        const val CANTEEN_UPI_ID = "yashshiraskar1@okhdfcbank"
        const val CANTEEN_NAME = "VES Canteen"
        const val UPI_REQUEST_CODE = 1001
    }

    private lateinit var tvPaymentTotal: TextView
    private lateinit var paymentItemsContainer: LinearLayout
    private lateinit var rbUPI: RadioButton
    private lateinit var rbCash: RadioButton
    private lateinit var cardUPI: MaterialCardView
    private lateinit var cardCash: MaterialCardView
    private lateinit var btnPayNow: MaterialButton

    private var selectedMethod = "cash" // "upi" or "cash"
    private var totalPrice = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        tvPaymentTotal = findViewById(R.id.tvPaymentTotal)
        paymentItemsContainer = findViewById(R.id.paymentItemsContainer)
        rbUPI = findViewById(R.id.rbUPI)
        rbCash = findViewById(R.id.rbCash)
        cardUPI = findViewById(R.id.cardUPI)
        cardCash = findViewById(R.id.cardCash)
        btnPayNow = findViewById(R.id.btnPayNow)

        // Load cart summary
        totalPrice = CartManager.getTotal()
        tvPaymentTotal.text = "₹${totalPrice.toInt()}"

        // Display cart items
        for (cartItem in CartManager.getItems()) {
            val tv = TextView(this).apply {
                text = "${cartItem.menuItem.name} × ${cartItem.quantity}  —  ₹${cartItem.getTotalPrice().toInt()}"
                textSize = 14f
                setTextColor(getColor(R.color.text_secondary))
                setPadding(0, 4, 0, 4)
            }
            paymentItemsContainer.addView(tv)
        }

        // Default selection: Cash
        rbCash.isChecked = true
        selectedMethod = "cash"
        updateSelectionUI()

        // Card click listeners for selection
        cardUPI.setOnClickListener { selectUPI() }
        rbUPI.setOnClickListener { selectUPI() }

        cardCash.setOnClickListener { selectCash() }
        rbCash.setOnClickListener { selectCash() }

        // Pay button
        btnPayNow.setOnClickListener {
            if (selectedMethod == "upi") {
                launchUPIPayment()
            } else {
                // Cash — go directly to order confirmation
                proceedToConfirmation("Cash at Counter")
            }
        }
    }

    private fun selectUPI() {
        selectedMethod = "upi"
        rbUPI.isChecked = true
        rbCash.isChecked = false
        btnPayNow.text = "Pay ₹${totalPrice.toInt()} via UPI"
        updateSelectionUI()
    }

    private fun selectCash() {
        selectedMethod = "cash"
        rbCash.isChecked = true
        rbUPI.isChecked = false
        btnPayNow.text = "Confirm Order (Cash)"
        updateSelectionUI()
    }

    private fun updateSelectionUI() {
        cardUPI.strokeColor = if (selectedMethod == "upi") getColor(R.color.primary) else getColor(R.color.divider)
        cardCash.strokeColor = if (selectedMethod == "cash") getColor(R.color.primary) else getColor(R.color.divider)
        cardUPI.strokeWidth = if (selectedMethod == "upi") 4 else 2
        cardCash.strokeWidth = if (selectedMethod == "cash") 4 else 2
    }

    /** Launch UPI payment intent — opens GPay, PhonePe, Paytm, etc. */
    private fun launchUPIPayment() {
        val uri = Uri.parse(
            "upi://pay?pa=$CANTEEN_UPI_ID" +
            "&pn=${Uri.encode(CANTEEN_NAME)}" +
            "&am=${totalPrice}" +
            "&cu=INR" +
            "&tn=${Uri.encode("VES Canteen Order")}"
        )

        val intent = Intent(Intent.ACTION_VIEW, uri)

        // Use try-catch instead of resolveActivity (fails on Android 11+)
        try {
            val chooser = Intent.createChooser(intent, "Pay with")
            startActivityForResult(chooser, UPI_REQUEST_CODE)
        } catch (e: Exception) {
            Toast.makeText(this, "No UPI app found! Please use Cash at Counter.",
                Toast.LENGTH_LONG).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == UPI_REQUEST_CODE) {
            // Parse UPI response
            val response = data?.getStringExtra("response") ?: ""

            when {
                // Payment successful — place order
                response.contains("SUCCESS", ignoreCase = true) -> {
                    proceedToConfirmation("UPI Payment ✓")
                }
                // Payment submitted but pending — DON'T place order
                response.contains("submitted", ignoreCase = true) -> {
                    Toast.makeText(this,
                        "Payment is pending. Please wait for confirmation or try again.",
                        Toast.LENGTH_LONG).show()
                }
                // Payment failed or cancelled — stay on payment screen
                else -> {
                    Toast.makeText(this,
                        "Payment failed or cancelled. Please try again.",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun proceedToConfirmation(paymentMethod: String) {
        val intent = Intent(this, OrderConfirmationActivity::class.java)
        intent.putExtra("paymentMethod", paymentMethod)
        startActivity(intent)
        finish()
    }
}
