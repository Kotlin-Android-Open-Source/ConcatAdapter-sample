package com.hoc.mergeadapter_sample

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hoc.mergeadapter_sample.databinding.ItemUserBinding

class UserAdapter :
  ListAdapter<User, UserAdapter.VH>(object : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User) = oldItem.uid == newItem.uid
    override fun areContentsTheSame(oldItem: User, newItem: User) = oldItem == newItem
  }) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
    ItemUserBinding.inflate(
      LayoutInflater.from(parent.context),
      parent,
      false
    )
  )

  override fun onBindViewHolder(holder: VH, position: Int) {
    holder.bind(getItem(position))
  }

  class VH(private val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: User) {
      binding.nameTextView.text = item.name
      binding.emailTextView.text = item.email
    }
  }
}