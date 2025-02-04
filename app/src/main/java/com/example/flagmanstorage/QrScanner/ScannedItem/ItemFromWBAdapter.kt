package com.example.flagmanstorage.QrScanner.ScannedItem

import android.annotation.SuppressLint
import android.text.Editable
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
    var onActionClickListener: ((ItemFromWB) -> Unit)? = null
    inner class ItemFromWBViewHolder(private val binding: ActivityItemFromWbBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ResourceAsColor")
        fun bind(item: ItemFromWB) {
            binding.textViewArticle.text = "${item.article} - ${item.count_cur} шт. / ${item.count_all} шт."
            binding.hiddenInput.text = Editable.Factory.getInstance().newEditable(item.id.toString())
            if(item.count_cur == item.count_all || item.status=="POSTPONED")
            {
                binding.buttonAction.isEnabled=false
                binding.buttonAction.setBackgroundColor(R.color.red_false)
            }
            binding.buttonAction.setOnClickListener {
                onActionClickListener?.invoke(item)
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

    private fun removeItem(position: Int) {
        Items.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, Items.size)
    }

    fun updateItems(newItems: List<ItemFromWB>) {
        Items.clear()
        Items.addAll(newItems)
        notifyDataSetChanged()  // Обновляем весь список
    }
}