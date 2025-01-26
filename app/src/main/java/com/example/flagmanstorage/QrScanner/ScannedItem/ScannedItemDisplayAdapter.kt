package com.example.flagmanstorage.QrScanner.ScannedItem

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.flagmanstorage.QrScanner.PreferencesHelper
import com.example.flagmanstorage.databinding.ActivityItemScannedBinding

class ScannedItemDisplayAdapter(
    private var scannedItems: MutableList<ScannedItemDisplay>,
    private val preferencesHelper: PreferencesHelper
) :
    RecyclerView.Adapter<ScannedItemDisplayAdapter.ScannedItemViewHolder>() {

    inner class ScannedItemViewHolder(private val binding: ActivityItemScannedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(scannedItem: ScannedItemDisplay) {
            binding.textViewCode.text = scannedItem.code
            binding.textCords.text = scannedItem.count.toString()
            binding.buttonAction.setOnClickListener {
                preferencesHelper.removeLastScannedItemByCode(scannedItem.code)
                if (scannedItem.count == 1) {
                    removeItem(adapterPosition)
                }
                updateScannedItems()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScannedItemViewHolder {
        val binding =
            ActivityItemScannedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    fun updateScannedItems() {
        scannedItems.clear()
        scannedItems.addAll(preferencesHelper.getGroupedScannedItems())  // Обновляем данные из SharedPreferences
        notifyDataSetChanged()  // Уведомляем адаптер об изменении данных
    }
}
