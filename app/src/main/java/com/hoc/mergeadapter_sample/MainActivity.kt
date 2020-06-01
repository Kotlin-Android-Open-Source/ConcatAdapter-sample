package com.hoc.mergeadapter_sample

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.MergeAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hoc.mergeadapter_sample.databinding.ActivityMainBinding
import kotlin.LazyThreadSafetyMode.NONE

class MainActivity : AppCompatActivity() {
  private val binding by lazy(NONE) { ActivityMainBinding.inflate(layoutInflater) }
  private val viewModel by viewModels<MainVM>(viewModelFactoryProducer)

  private val userAdapter = UserAdapter()
  private val footerAdapter = FooterAdapter(this::onRetry)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(binding.root)

    binding.recyclerView.run {
      setHasFixedSize(true)
      val linearLayoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
      layoutManager = linearLayoutManager
      adapter = MergeAdapter(userAdapter, footerAdapter)

      addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
          if (dy > 0
            && linearLayoutManager.findLastVisibleItemPosition() + VISIBLE_THRESHOLD >= linearLayoutManager.itemCount
          ) {
            viewModel.loadNextPage()
          }
        }
      })
    }

    viewModel.loadingStateLiveData.observe(this, Observer(footerAdapter::submitList))
    viewModel.userLiveData.observe(this, Observer(userAdapter::submitList))
  }

  private fun onRetry() = viewModel.retryNextPage()

  private companion object {
    private const val VISIBLE_THRESHOLD = 3
  }
}
