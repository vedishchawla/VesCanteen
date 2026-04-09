package com.example.vescanteen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Order Confirmation Activity - Shows order success with token number.
 * Saves order to Firestore, shows notification, and clears the cart.
 */
class OrderConfirmationActivity : AppCompatActivity() {

    private lateinit var tvTokenNumber: TextView
    private lateinit var tvOrderTime: TextView
    private lateinit var tvOrderTotal: TextView
    private lateinit var orderItemsContainer: LinearLayout
    private lateinit var btnBackHome: MaterialButton

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_confirmation)

        tvTokenNumber = findViewById(R.id.tvTokenNumber)
        tvOrderTime = findViewById(R.id.tvOrderTime)
        tvOrderTotal = findViewById(R.id.tvOrderTotal)
        orderItemsContainer = findViewById(R.id.orderItemsContainer)
        btnBackHome = findViewById(R.id.btnBackHome)

        // Request notification permission on Android 13+
        requestNotificationPermission()

        // Create notification channel
        NotificationHelper.createChannel(this)

        // Generate token number (random 3-digit)
        val tokenNumber = (100..999).random()

        // Get current time
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val currentTime = timeFormat.format(Date())

        // Get cart details before clearing
        val cartItems = CartManager.getItems()
        val totalPrice = CartManager.getTotal()

        // Display order details
        tvTokenNumber.text = "#$tokenNumber"
        tvOrderTime.text = currentTime
        tvOrderTotal.text = "₹${totalPrice.toInt()}"

        // Display order items
        for (cartItem in cartItems) {
            val itemView = layoutInflater.inflate(android.R.layout.simple_list_item_1, orderItemsContainer, false)
            val tvItem = itemView.findViewById<TextView>(android.R.id.text1)
            tvItem.text = "${cartItem.menuItem.name} × ${cartItem.quantity}  —  ₹${cartItem.getTotalPrice().toInt()}"
            tvItem.textSize = 14f
            tvItem.setPadding(0, 4, 0, 4)
            orderItemsContainer.addView(itemView)
        }

        // Save order to Firestore
        saveOrderToFirestore(tokenNumber, cartItems, totalPrice)

        // 🔔 Show notification
        NotificationHelper.showOrderConfirmation(this, tokenNumber, totalPrice)

        // Clear cart after order is placed
        CartManager.clearCart()

        // Back to menu
        btnBackHome.setOnClickListener {
            finish()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }

    private fun saveOrderToFirestore(
        tokenNumber: Int,
        cartItems: List<com.example.vescanteen.model.CartItem>,
        totalPrice: Double
    ) {
        val user = auth.currentUser ?: return

        val orderItems = cartItems.map { cartItem ->
            hashMapOf(
                "name" to cartItem.menuItem.name,
                "price" to cartItem.menuItem.price,
                "quantity" to cartItem.quantity,
                "total" to cartItem.getTotalPrice()
            )
        }

        val order = hashMapOf(
            "userId" to user.uid,
            "items" to orderItems,
            "totalPrice" to totalPrice,
            "tokenNumber" to tokenNumber,
            "status" to "confirmed",
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("orders")
            .add(order)
            .addOnSuccessListener { /* Order saved */ }
            .addOnFailureListener { /* Handle error silently */ }
    }
}
