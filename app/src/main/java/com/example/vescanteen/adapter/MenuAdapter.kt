package com.example.vescanteen.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.vescanteen.CartManager
import com.example.vescanteen.R
import com.example.vescanteen.model.MenuItem
import com.google.android.material.button.MaterialButton

/**
 * RecyclerView Adapter for menu item cards in the grid.
 * Shows ADD button → transforms to  - 1 +  stepper when items added.
 */
class MenuAdapter(
    private var items: List<MenuItem>,
    private val onCartChanged: () -> Unit
) : RecyclerView.Adapter<MenuAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivFoodImage: ImageView = view.findViewById(R.id.ivFoodImage)
        val tvFoodName: TextView = view.findViewById(R.id.tvFoodName)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val btnAdd: MaterialButton = view.findViewById(R.id.btnAdd)
        val quantityStepper: LinearLayout = view.findViewById(R.id.quantityStepper)
        val btnStepperMinus: TextView = view.findViewById(R.id.btnStepperMinus)
        val tvStepperCount: TextView = view.findViewById(R.id.tvStepperCount)
        val btnStepperPlus: TextView = view.findViewById(R.id.btnStepperPlus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        holder.tvFoodName.text = item.name
        holder.tvPrice.text = "₹${item.price.toInt()}"

        // Show correct state: ADD button or stepper
        updateStepperState(holder, item)

        // Load image
        if (item.drawableResName.isNotEmpty()) {
            val resId = context.resources.getIdentifier(
                item.drawableResName, "drawable", context.packageName
            )
            if (resId != 0) {
                Glide.with(context)
                    .load(resId)
                    .transform(CenterCrop(), RoundedCorners(24))
                    .into(holder.ivFoodImage)
            } else {
                holder.ivFoodImage.setImageResource(R.drawable.ic_food)
            }
        } else if (item.imageUrl.isNotEmpty()) {
            Glide.with(context)
                .load(item.imageUrl)
                .placeholder(R.drawable.ic_food)
                .transform(CenterCrop(), RoundedCorners(24))
                .into(holder.ivFoodImage)
        } else {
            holder.ivFoodImage.setImageResource(R.drawable.ic_food)
        }

        // ADD button — first tap adds 1
        holder.btnAdd.setOnClickListener {
            CartManager.addItem(item)
            updateStepperState(holder, item)
            onCartChanged()
        }

        // Stepper +
        holder.btnStepperPlus.setOnClickListener {
            CartManager.addItem(item)
            updateStepperState(holder, item)
            onCartChanged()
        }

        // Stepper -
        holder.btnStepperMinus.setOnClickListener {
            CartManager.removeItem(item.id)
            updateStepperState(holder, item)
            onCartChanged()
        }
    }

    /** Toggle between ADD button and - count + stepper */
    private fun updateStepperState(holder: ViewHolder, item: MenuItem) {
        val qty = CartManager.getItemQuantity(item.id)
        if (qty > 0) {
            holder.btnAdd.visibility = View.GONE
            holder.quantityStepper.visibility = View.VISIBLE
            holder.tvStepperCount.text = qty.toString()
        } else {
            holder.btnAdd.visibility = View.VISIBLE
            holder.quantityStepper.visibility = View.GONE
        }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<MenuItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
