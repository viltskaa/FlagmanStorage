package com.example.flagmanstorage.QrScanner.ScannedItem

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flagmanstorage.databinding.ActivityOrderFromWbBinding
import com.example.flagmanstorage.databinding.ActivityItemFromWbBinding

class OrderFromWbAdapter(private val orders: MutableList<OrderFromWb>) :
    RecyclerView.Adapter<OrderFromWbAdapter.OrderViewHolder>() {
    var onActionClickListener: ((OrderFromWb) -> Unit)? = null
    inner class OrderViewHolder(private val binding: ActivityOrderFromWbBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("ResourceAsColor")
        fun bind(order: OrderFromWb) {
            binding.textViewOrderUid.text = order.orderUid

            val itemAdapter = ItemFromWBAdapter(order.items.toMutableList())
            binding.recyclerViewItems.layoutManager = LinearLayoutManager(binding.root.context)
            binding.recyclerViewItems.adapter = itemAdapter
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ActivityOrderFromWbBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int = orders.size

    fun updateItems(newOrders: List<OrderFromWb>) {
        orders.clear()
        orders.addAll(newOrders)
        notifyDataSetChanged()
    }
}
