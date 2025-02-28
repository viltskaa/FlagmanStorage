package com.example.flagmanstorage.QrScanner.ScannedItem

import android.annotation.SuppressLint
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flagmanstorage.QrScanner.PreferencesHelper
import com.example.flagmanstorage.R
import com.example.flagmanstorage.databinding.ActivityItemFromWbBinding

class ItemFromWBAdapter(private var Items: MutableList<ItemFromWB>) :
    RecyclerView.Adapter<ItemFromWBAdapter.ItemFromWBViewHolder>() {
    inner class ItemFromWBViewHolder(private val binding: ActivityItemFromWbBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ResourceAsColor")
        fun bind(item: ItemFromWB) {
            binding.textViewArticle.text = "${item.article}"
            binding.textForThis.text = item.for_this
            binding.hiddenInput.text = Editable.Factory.getInstance().newEditable(item.id.toString())
            if (item.scanned == "NOTSCANNED"){
                binding.imageViewItem.setImageResource(R.drawable.notscanned)
            }
            else if(item.scanned == "SCANNED"){
                binding.imageViewItem.setImageResource(R.drawable.scanned)
            }

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
}