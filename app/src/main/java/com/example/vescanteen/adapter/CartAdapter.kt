package com.example.vescanteen.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.vescanteen.R
import com.example.vescanteen.model.CartItem

/**
 * RecyclerView Adapter for cart items.
 */
class CartAdapter(
    private var items: List<CartItem>,
    private val onQuantityChange: () -> Unit
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivCartImage: ImageView = view.findViewById(R.id.ivCartImage)
        val tvCartItemName: TextView = view.findViewById(R.id.tvCartItemName)
        val tvCartItemPrice: TextView = view.findViewById(R.id.tvCartItemPrice)
        val tvQuantity: TextView = view.findViewById(R.id.tvQuantity)
        val btnMinus: ImageView = view.findViewById(R.id.btnMinus)
        val btnPlus: ImageView = view.findViewById(R.id.btnPlus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cartItem = items[position]
        val context = holder.itemView.context

        holder.tvCartItemName.text = cartItem.menuItem.name
        holder.tvCartItemPrice.text = "₹${cartItem.getTotalPrice().toInt()}"
        holder.tvQuantity.text = cartItem.quantity.toString()

        // Load image from drawable or URL
        if (cartItem.menuItem.drawableResName.isNotEmpty()) {
            val resId = context.resources.getIdentifier(
                cartItem.menuItem.drawableResName, "drawable", context.packageName
            )
            if (resId != 0) {
                Glide.with(context)
                    .load(resId)
                    .transform(CenterCrop(), RoundedCorners(16))
                    .into(holder.ivCartImage)
            } else {
                holder.ivCartImage.setImageResource(R.drawable.ic_food)
            }
        } else if (cartItem.menuItem.imageUrl.isNotEmpty()) {
            Glide.with(context)
                .load(cartItem.menuItem.imageUrl)
                .placeholder(R.drawable.ic_food)
                .transform(CenterCrop(), RoundedCorners(16))
                .into(holder.ivCartImage)
        } else {
            holder.ivCartImage.setImageResource(R.drawable.ic_food)
        }

        holder.btnPlus.setOnClickListener {
            cartItem.quantity++
            notifyItemChanged(position)
            onQuantityChange()
        }

        holder.btnMinus.setOnClickListener {
            if (cartItem.quantity > 1) {
                cartItem.quantity--
                notifyItemChanged(position)
            } else {
                val mutableItems = items.toMutableList()
                mutableItems.removeAt(position)
                items = mutableItems
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, items.size)
            }
            onQuantityChange()
        }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<CartItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
