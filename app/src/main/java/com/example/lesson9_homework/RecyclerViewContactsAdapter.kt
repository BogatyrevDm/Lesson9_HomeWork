package com.example.lesson9_homework

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lesson9_homework.databinding.ItemViewContactsBinding


class RecyclerViewContactsAdapter :
    RecyclerView.Adapter<RecyclerViewContactsAdapter.ContactsViewHolder>() {
    private var dataSource = listOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsViewHolder {
        val binding =
            ItemViewContactsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactsViewHolder(binding)
    }

    fun setData(data: List<String>) {
        dataSource = data
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        holder.onBind(dataSource[position])
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    inner class ContactsViewHolder(val binding: ItemViewContactsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(stringItem: String) {
            binding.contactsRvItem.text = stringItem
        }
    }

}