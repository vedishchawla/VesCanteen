package com.example.vescanteen

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vescanteen.adapter.CartAdapter
import com.google.android.material.button.MaterialButton

/**
 * Cart Fragment - Shows items in cart with quantity controls.
 * Bottom bar shows total and checkout button.
 *
 * Exp 4: Gesture handling — Swipe-to-delete cart items (ItemTouchHelper).
 * Exp A1: Sensor integration — Shake to clear cart (Accelerometer).
 */
class CartFragment : Fragment() {

    private lateinit var rvCart: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var bottomBar: LinearLayout
    private lateinit var tvTotal: TextView
    private lateinit var btnCheckout: MaterialButton
    private lateinit var cartAdapter: CartAdapter

    // Exp A1: Shake detector
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var shakeDetector: ShakeDetector? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvCart = view.findViewById(R.id.rvCart)
        emptyState = view.findViewById(R.id.emptyState)
        bottomBar = view.findViewById(R.id.bottomBar)
        tvTotal = view.findViewById(R.id.tvTotal)
        btnCheckout = view.findViewById(R.id.btnCheckout)

        // Setup RecyclerView
        cartAdapter = CartAdapter(CartManager.getItems()) {
            updateUI()
        }
        rvCart.layoutManager = LinearLayoutManager(context)
        rvCart.adapter = cartAdapter

        // Exp 4: Swipe-to-delete gesture
        setupSwipeToDelete()

        // Exp A1: Setup shake detector (accelerometer sensor)
        setupShakeDetector()

        // Checkout button
        btnCheckout.setOnClickListener {
            if (CartManager.getItemCount() == 0) {
                Toast.makeText(context, "Cart is empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Navigate to payment screen
            val intent = Intent(context, PaymentActivity::class.java)
            startActivity(intent)
        }

        updateUI()
    }

    override fun onResume() {
        super.onResume()
        cartAdapter.updateItems(CartManager.getItems())
        updateUI()

        // Exp A1: Register accelerometer listener
        accelerometer?.let { sensor ->
            sensorManager?.registerListener(shakeDetector, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        // Exp A1: Unregister to save battery
        sensorManager?.unregisterListener(shakeDetector)
    }

    /**
     * Exp 4: Swipe-to-delete using ItemTouchHelper.
     * User swipes left on a cart item to delete it.
     */
    private fun setupSwipeToDelete() {
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false  // No drag-and-drop

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val items = CartManager.getItems()
                if (position < items.size) {
                    val removedItem = items[position]
                    CartManager.deleteItem(removedItem.menuItem.id)
                    cartAdapter.updateItems(CartManager.getItems())
                    updateUI()
                    Toast.makeText(context, "${removedItem.menuItem.name} removed", Toast.LENGTH_SHORT).show()
                }
            }
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(rvCart)
    }

    /**
     * Exp A1: Shake detection using accelerometer sensor.
     * Shaking the phone while on cart screen clears all items.
     */
    private fun setupShakeDetector() {
        sensorManager = context?.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (accelerometer == null) {
            // Device doesn't have accelerometer
            return
        }

        shakeDetector = ShakeDetector {
            // Shake detected — confirm before clearing
            activity?.runOnUiThread {
                if (CartManager.getItemCount() > 0) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("🗑 Clear Cart?")
                        .setMessage("You shook the device! Do you want to clear your entire cart?")
                        .setPositiveButton("Clear") { _, _ ->
                            CartManager.clearCart()
                            cartAdapter.updateItems(CartManager.getItems())
                            updateUI()
                            Toast.makeText(context, "Cart cleared! 🗑", Toast.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }
        }
    }

    private fun updateUI() {
        val items = CartManager.getItems()
        if (items.isEmpty()) {
            rvCart.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
            bottomBar.visibility = View.GONE
        } else {
            rvCart.visibility = View.VISIBLE
            emptyState.visibility = View.GONE
            bottomBar.visibility = View.VISIBLE
            tvTotal.text = "₹${CartManager.getTotal().toInt()}"
        }
    }
}
