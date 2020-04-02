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
  private val vm by viewModels<MainVM>()

  private val userAdapter = UserAdapter()
  private val footerAdapter = FooterAdapter(this::onRetry)

  private val binding by lazy(NONE) {
    ActivityMainBinding.inflate(layoutInflater)
  }

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
            vm.loadNextPage()
          }
        }
      })
    }


    vm.loadingStateLiveData.observe(this, Observer {
      footerAdapter.submitList(it)
    })
    vm.userLiveData.observe(this, Observer {
      userAdapter.submitList(it)
    })
    vm.loadNextPage()
  }

  private fun onRetry() = vm.retryNextPage()

  private companion object {
    private const val VISIBLE_THRESHOLD = 3
  }
}
