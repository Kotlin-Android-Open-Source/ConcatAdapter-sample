package com.hoc.mergeadapter_sample

import androidx.annotation.MainThread
import androidx.lifecycle.*
import kotlinx.coroutines.launch


class MainVM : ViewModel() {
  private val usersD = MutableLiveData<List<User>>().apply { value = emptyList() }
  val userLiveData: LiveData<List<User>> get() = usersD

  private val loadingStateD =
    MutableLiveData<PlaceholderState>().apply { value = PlaceholderState.Idle }
  val loadingStateLiveData: LiveData<List<LoadingState>>
    get() = loadingStateD.map {
      when (it) {
        null -> emptyList()
        PlaceholderState.Idle -> emptyList()
        PlaceholderState.Loading -> listOf(LoadingState.Loading)
        is PlaceholderState.Error -> listOf(LoadingState.Error(it.throwable))
      }
    }

  @MainThread
  fun loadNextPage() {
    val state = loadingStateD.value
    if (state === null || state is PlaceholderState.Idle) {
      _loadNextPage()
    }
  }

  @MainThread
  fun retryNextPage() {
    if (loadingStateD.value is PlaceholderState.Error) {
      _loadNextPage()
    }
  }

  @Suppress("FunctionName")
  private fun _loadNextPage() {
    viewModelScope.launch {
      loadingStateD.value = PlaceholderState.Loading

      val currentList = usersD.value ?: emptyList()
      kotlin.runCatching { getUsers(start = currentList.size, limit = LIMIT) }
        .fold(
          onSuccess = {
            usersD.value = currentList + it
            loadingStateD.value = PlaceholderState.Idle
          },
          onFailure = {
            loadingStateD.value = PlaceholderState.Error(it)
          }
        )
    }
  }

  private sealed class PlaceholderState {
    object Idle : PlaceholderState()
    object Loading : PlaceholderState()
    data class Error(val throwable: Throwable) : PlaceholderState()
  }

  private companion object {
    const val LIMIT = 20
  }
}