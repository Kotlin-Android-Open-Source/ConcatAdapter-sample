package com.hoc.mergeadapter_sample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hoc.mergeadapter_sample.databinding.ItemFooterBinding

class FooterAdapter(private val onRetry: () -> Unit) :
  ListAdapter<LoadingState, FooterAdapter.VH>(object : DiffUtil.ItemCallback<LoadingState>() {
    override fun areItemsTheSame(oldItem: LoadingState, newItem: LoadingState) = true
    override fun areContentsTheSame(oldItem: LoadingState, newItem: LoadingState) =
      oldItem == newItem
  }) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
    ItemFooterBinding.inflate(
      LayoutInflater.from(parent.context),
      parent,
      false
    )
  )

  override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

  inner class VH(private val binding: ItemFooterBinding) : RecyclerView.ViewHolder(binding.root) {
    init {
      binding.buttonRetry.setOnClickListener {
        onRetry()
      }
    }

    fun bind(item: LoadingState) {
      when (item) {
        LoadingState.Loading -> {
          binding.run {
            buttonRetry.visibility = View.INVISIBLE
            textViewError.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE
          }
        }
        is LoadingState.Error -> {
          binding.run {
            buttonRetry.visibility = View.VISIBLE
            textViewError.visibility = View.VISIBLE
            textViewError.text = item.throwable.message
            progressBar.visibility = View.INVISIBLE
          }
        }
      }
    }
  }
}