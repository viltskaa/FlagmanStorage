package com.example.flagmanstorage.QrScanner.ScannedItem

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.flagmanstorage.QrScanner.PreferencesHelper
import com.example.flagmanstorage.databinding.ActivityItemScannedBinding

class ScannedItemAdapter(private var scannedItems: MutableList<ScannedItem>, private val preferencesHelper: PreferencesHelper) :
    RecyclerView.Adapter<ScannedItemAdapter.ScannedItemViewHolder>() {

    inner class ScannedItemViewHolder(private val binding: ActivityItemScannedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(scannedItem: ScannedItem) {
            binding.textViewCode.text = scannedItem.code
            binding.textViewTime.text = java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(scannedItem.timestamp)
            binding.textCords.text="X: " + scannedItem.positionX.toString() + " Y:" + scannedItem.positionY.toString() + " Z:" + scannedItem.positionZ.toString()
            binding.buttonAction.setOnClickListener {
                preferencesHelper.removeScannedItem(scannedItem)
                removeItem(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScannedItemViewHolder {
        val binding = ActivityItemScannedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScannedItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScannedItemViewHolder, position: Int) {
        holder.bind(scannedItems[position])
    }

    override fun getItemCount(): Int = scannedItems.size
    private fun removeItem(position: Int) {
        scannedItems.removeAt(position)  // Удаляем элемент из списка
        notifyItemRemoved(position)      // Уведомляем адаптер об удалении элемента
        notifyItemRangeChanged(position, scannedItems.size)  // Обновляем оставшиеся элементы
    }
}