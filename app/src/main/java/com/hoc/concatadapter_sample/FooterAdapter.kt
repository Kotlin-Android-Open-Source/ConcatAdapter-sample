package com.hoc.concatadapter_sample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hoc.concatadapter_sample.databinding.ItemFooterBinding

class FooterAdapter(private val onRetry: () -> Unit) :
  ListAdapter<PlaceholderState, FooterAdapter.VH>(object :
    DiffUtil.ItemCallback<PlaceholderState>() {
    override fun areItemsTheSame(oldItem: PlaceholderState, newItem: PlaceholderState) = true
    override fun areContentsTheSame(oldItem: PlaceholderState, newItem: PlaceholderState) =
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

    fun bind(item: PlaceholderState) {
      when (item) {
        PlaceholderState.Loading -> {
          binding.run {
            buttonRetry.visibility = View.INVISIBLE
            textViewError.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE
          }
        }
        is PlaceholderState.Failure -> {
          binding.run {
            buttonRetry.visibility = View.VISIBLE
            textViewError.visibility = View.VISIBLE
            textViewError.text = item.throwable.message
            progressBar.visibility = View.INVISIBLE
          }
        }
        PlaceholderState.Idle -> error("Should not be here!")
      }
    }
  }
}