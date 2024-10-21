package com.example.flagmanstorage.QrScanner.ScannedItem

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.flagmanstorage.QrScanner.PreferencesHelper
import com.example.flagmanstorage.databinding.ActivityItemFromWbBinding

class ItemFromWBAdapter(private var Items: MutableList<ItemFromWB>, private val preferencesHelper: PreferencesHelper) :
    RecyclerView.Adapter<ItemFromWBAdapter.ItemFromWBViewHolder>() {

    inner class ItemFromWBViewHolder(private val binding: ActivityItemFromWbBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ItemFromWB) {
            binding.textViewArticle.text = "${item.article} - ${item.count} шт."  // Отображаем артикул и количество
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemFromWBViewHolder {
        val binding = ActivityItemFromWbBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemFromWBViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemFromWBViewHolder, position: Int) {
        holder.bind(Items[position])
    }

    override fun getItemCount(): Int = Items.size

    private fun removeItem(position: Int) {
        Items.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, Items.size)
    }
}