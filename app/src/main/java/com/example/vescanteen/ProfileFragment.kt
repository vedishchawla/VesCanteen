package com.example.vescanteen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vescanteen.database.OrderHistoryDatabase
import com.example.vescanteen.database.OrderHistoryEntity
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Profile Fragment - Shows user info, order history, GPS canteen finder.
 *
 * Exp 5: SharedPreferences for user preference display.
 * Exp 7: Room Database — displays local order history.
 * Exp A1: GPS sensor — calculates distance to VESIT canteen.
 */
class ProfileFragment : Fragment() {

    companion object {
        // VESIT Canteen coordinates (Chembur, Mumbai)
        private const val CANTEEN_LAT = 19.0449
        private const val CANTEEN_LNG = 72.8892
        private const val LOCATION_PERMISSION_REQUEST = 2001
    }

    private lateinit var tvAvatar: TextView
    private lateinit var tvProfileName: TextView
    private lateinit var tvProfileEmail: TextView
    private lateinit var tvProfilePhone: TextView
    private lateinit var tvProfileUid: TextView
    private lateinit var btnLogout: MaterialButton
    private lateinit var btnFindCanteen: MaterialButton
    private lateinit var rvOrderHistory: RecyclerView
    private lateinit var tvNoOrders: TextView

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
        btnFindCanteen = view.findViewById(R.id.btnFindCanteen)
        rvOrderHistory = view.findViewById(R.id.rvOrderHistory)
        tvNoOrders = view.findViewById(R.id.tvNoOrders)

        // Load user data
        loadUserProfile()

        // Exp 7: Load order history from Room DB
        loadOrderHistory()

        // Exp A1: Find canteen using GPS
        btnFindCanteen.setOnClickListener {
            findCanteenDistance()
        }

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

    /**
     * Exp 7: Load order history from Room Database.
     * Shows past orders stored locally (works offline).
     */
    private fun loadOrderHistory() {
        Thread {
            val database = OrderHistoryDatabase.getDatabase(requireContext())
            val orders = database.orderHistoryDao().getAllOrders()

            activity?.runOnUiThread {
                if (orders.isEmpty()) {
                    rvOrderHistory.visibility = View.GONE
                    tvNoOrders.visibility = View.VISIBLE
                } else {
                    rvOrderHistory.visibility = View.VISIBLE
                    tvNoOrders.visibility = View.GONE
                    rvOrderHistory.layoutManager = LinearLayoutManager(context)
                    rvOrderHistory.adapter = OrderHistoryAdapter(orders)
                }
            }
        }.start()
    }

    /**
     * Exp A1: GPS sensor — Get current location and calculate distance to canteen.
     */
    private fun findCanteenDistance() {
        val ctx = context ?: return

        // Check location permission
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
            return
        }

        // Get last known location
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                // Calculate distance to canteen
                val canteenLocation = Location("canteen").apply {
                    latitude = CANTEEN_LAT
                    longitude = CANTEEN_LNG
                }
                val distanceMeters = location.distanceTo(canteenLocation)
                val distanceKm = distanceMeters / 1000

                val message = if (distanceKm < 1) {
                    "📍 You are ${distanceMeters.toInt()}m away from VES Canteen\n\n" +
                    "🏃 Walk time: ~${(distanceMeters / 80).toInt()} min\n" +
                    "📌 VESIT, Chembur, Mumbai"
                } else {
                    "📍 You are ${"%.1f".format(distanceKm)}km away from VES Canteen\n\n" +
                    "🚗 Drive time: ~${(distanceKm * 3).toInt()} min\n" +
                    "📌 VESIT, Collector's Colony, Chembur"
                }

                AlertDialog.Builder(requireContext())
                    .setTitle("🍽 VES Canteen Location")
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .show()
            } else {
                Toast.makeText(ctx, "📍 Unable to get location. Try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST &&
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            findCanteenDistance()
        } else {
            Toast.makeText(context, "Location permission needed to find canteen", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Simple RecyclerView adapter for order history items.
     */
    inner class OrderHistoryAdapter(
        private val orders: List<OrderHistoryEntity>
    ) : RecyclerView.Adapter<OrderHistoryAdapter.VH>() {

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val tvToken: TextView = view.findViewById(android.R.id.text1)
            val tvDetails: TextView = view.findViewById(android.R.id.text2)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_2, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val order = orders[position]
            val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            val date = dateFormat.format(Date(order.timestamp))

            holder.tvToken.text = "Token #${order.tokenNumber}  •  ₹${order.totalPrice.toInt()}"
            holder.tvToken.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
            holder.tvDetails.text = "${order.items}  •  $date  •  ${order.paymentMethod}"
        }

        override fun getItemCount() = orders.size
    }
}
