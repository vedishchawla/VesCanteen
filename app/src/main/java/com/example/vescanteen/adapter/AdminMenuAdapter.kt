package com.example.vescanteen.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vescanteen.R

/**
 * Adapter for admin menu management list.
 * Shows item name, details, and delete button.
 */
class AdminMenuAdapter(
    private val items: List<Triple<String, String, String>>,
    private val onDelete: (String) -> Unit
) : RecyclerView.Adapter<AdminMenuAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvAdminItemName)
        val tvDetails: TextView = view.findViewById(R.id.tvAdminItemDetails)
        val btnDelete: ImageView = view.findViewById(R.id.btnDeleteItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_menu, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (docId, name, details) = items[position]
        holder.tvName.text = name
        holder.tvDetails.text = details
        holder.btnDelete.setOnClickListener { onDelete(docId) }
    }

    override fun getItemCount() = items.size
}
