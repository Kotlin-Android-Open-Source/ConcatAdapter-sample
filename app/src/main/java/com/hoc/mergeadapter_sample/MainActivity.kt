package com.hoc.mergeadapter_sample

import android.os.Bundle
import android.view.View
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
  private val footerAdapter = FooterAdapter(::onRetry)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(binding.root)

    val linearLayoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

    binding.recyclerView.run {
      setHasFixedSize(true)
      layoutManager = linearLayoutManager
      adapter = MergeAdapter(userAdapter, footerAdapter)
    }

    // observe livedatas
    viewModel.firstPageStateLiveData.observe(this, Observer(::renderFirstPageState))
    viewModel.loadingStateLiveData.observe(this, Observer(footerAdapter::submitList))
    viewModel.userLiveData.observe(this, Observer(userAdapter::submitList))
    viewModel.isRefreshingLiveData.observe(this, Observer {
      binding.swipeRefreshLayout.run {
        if (it) {
          post { isRefreshing = true }
        } else {
          isRefreshing = false
        }
      }
    })

    // bind action
    binding.run {
      swipeRefreshLayout.setOnRefreshListener { viewModel.refresh() }
      recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
          if (dy > 0
            && linearLayoutManager.findLastVisibleItemPosition() + VISIBLE_THRESHOLD >= linearLayoutManager.itemCount
          ) {
            viewModel.loadNextPage()
          }
        }
      })
      retryButton.setOnClickListener { viewModel.retryNextPage() }
    }
  }

  private fun renderFirstPageState(state: PlaceholderState) = binding.run {
    when (state) {
      PlaceholderState.Idle -> {
        errorGroup.visibility = View.GONE
        progressBar.visibility = View.INVISIBLE
      }
      PlaceholderState.Loading -> {
        errorGroup.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
      }
      is PlaceholderState.Failure -> {
        errorGroup.visibility = View.VISIBLE
        progressBar.visibility = View.INVISIBLE
        errorText.text = state.throwable.message
      }
    }
  }

  private fun onRetry() = viewModel.retryNextPage()

  private companion object {
    private const val VISIBLE_THRESHOLD = 3
  }
}
