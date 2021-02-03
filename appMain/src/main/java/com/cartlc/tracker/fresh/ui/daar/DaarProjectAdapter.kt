package com.cartlc.tracker.fresh.ui.daar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cartlc.tracker.R

class DaarProjectAdapter(
        private val layoutInflater: LayoutInflater,
        private val items: List<String>,
        private val listener: Listener
) : RecyclerView.Adapter<DaarProjectAdapter.ViewHolder>() {

    var selectedItem: Int = -1

    interface Listener {
        fun onProjectItemSelected(position: Int, item: String)
        fun isSelected(position: Int, item: String): Boolean
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(android.R.id.text1)
    }

    // region DaarProjectAdapter

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            val value = items[position]
            textView.isSelected = position == selectedItem
            textView.text = value
            textView.setOnClickListener { onItemSelected(value, position) }
            if (listener.isSelected(position, value)) {
                textView.setBackgroundResource(R.color.selected)
            } else {
                textView.setBackgroundResource(android.R.color.transparent)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    // endregion DaarProjectAdapter

    private fun onItemSelected(text: String, position: Int) {
        if (selectedItem >= 0) {
            notifyItemChanged(selectedItem)
        }
        selectedItem = position
        notifyItemChanged(position)
        listener.onProjectItemSelected(position, text)
    }
}