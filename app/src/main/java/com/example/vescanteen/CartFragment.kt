package com.example.vescanteen

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vescanteen.adapter.CartAdapter
import com.google.android.material.button.MaterialButton

/**
 * Cart Fragment - Shows items in cart with quantity controls.
 * Bottom bar shows total and checkout button.
 */
class CartFragment : Fragment() {

    private lateinit var rvCart: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var bottomBar: LinearLayout
    private lateinit var tvTotal: TextView
    private lateinit var btnCheckout: MaterialButton
    private lateinit var cartAdapter: CartAdapter

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
