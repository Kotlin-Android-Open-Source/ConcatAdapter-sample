package com.hoc.mergeadapter_sample

import androidx.annotation.MainThread
import androidx.lifecycle.*
import com.hoc.mergeadapter_sample.PlaceholderState.*
import kotlinx.coroutines.launch
import kotlin.LazyThreadSafetyMode.NONE

sealed class PlaceholderState {
  object Idle : PlaceholderState()
  object Loading : PlaceholderState()
  data class Failure(val throwable: Throwable) : PlaceholderState()
}

class MainVM(
  private val getUsers: suspend (start: Int, limit: Int) -> List<User>
) : ViewModel() {

  //region Private fields
  private val usersD by lazy(NONE) {
    MutableLiveData<List<User>>()
      .apply { value = emptyList() }
      .also { loadNextPage() }
  }
  private val loadingStateD = MutableLiveData<PlaceholderState>().apply { value = Idle }
  private val firstPageStateD = MutableLiveData<PlaceholderState>().apply { value = Idle }
  private val isRefreshingD = MutableLiveData<Boolean>().apply { value = false }

  private var isFirstPage = true
  private var loadedAllPage = false

  private val shouldLoadNextPage: Boolean
    get() = if (isFirstPage) {
      firstPageStateD.value!! == Idle
    } else {
      loadingStateD.value!! == Idle
    } && !loadedAllPage

  private val shouldRetryNextPage: Boolean
    get() = if (isFirstPage) {
      firstPageStateD.value!! is Failure
    } else {
      loadingStateD.value!! is Failure
    }

  //endregion

  //region Public LiveDatas

  val userLiveData: LiveData<List<User>> get() = usersD

  val firstPageStateLiveData: LiveData<PlaceholderState> get() = firstPageStateD

  val loadingStateLiveData: LiveData<List<PlaceholderState>>
    get() = loadingStateD.map { if (it == Idle) emptyList() else listOf(it) }

  val isRefreshingLiveData: LiveData<Boolean> get() = isRefreshingD

  //endregion

  //region Public methods
  @MainThread
  fun loadNextPage() {
    if (shouldLoadNextPage) {
      loadPageInternal()
    }
  }

  @MainThread
  fun retryNextPage() {
    if (shouldRetryNextPage) {
      loadPageInternal()
    }
  }

  @MainThread
  fun refresh() {
    loadPageInternal(refresh = true)
  }
  //endregion

  //region Private methods
  private fun updateState(state: PlaceholderState) {
    if (isFirstPage) {
      firstPageStateD.value = state
    } else {
      loadingStateD.value = state
    }
  }

  private fun loadPageInternal(refresh: Boolean = false) {
    viewModelScope.launch {
      if (refresh) {
        isRefreshingD.value = true
      } else {
        updateState(Loading)
      }

      val currentList = if (refresh) emptyList() else usersD.value!!

      runCatching { getUsers(currentList.size, LIMIT) }
        .fold(
          onSuccess = {
            if (refresh) {
              isRefreshingD.value = false
            } else {
              updateState(Idle)
            }
            usersD.value = currentList + it

            isFirstPage = false
            loadedAllPage = it.isEmpty()
          },
          onFailure = {
            if (refresh) {
              isRefreshingD.value = false
            } else {
              updateState(Failure(it))
            }
          }
        )
    }
  }
  //endregion

  private companion object {
    const val LIMIT = 20
  }
}