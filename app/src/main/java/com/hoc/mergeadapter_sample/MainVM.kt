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

class MainVM(private val getUsers: suspend (start: Int, limit: Int) -> List<User>) : ViewModel() {

  //region Private
  private val usersD by lazy(NONE) {
    MutableLiveData<List<User>>()
      .apply { value = emptyList() }
      .also { loadNextPage() /* load first page when first accessing*/ }
  }
  private val loadingStateD = MutableLiveData<PlaceholderState>().apply { value = Idle }
  private val firstPageStateD = MutableLiveData<PlaceholderState>().apply { value = Idle }

  private var isFirstPage = true

  private val shouldLoadNextPage: Boolean
    get() = if (isFirstPage) {
      firstPageStateD.value!! == Idle
    } else {
      loadingStateD.value!! == Idle
    }

  private val shouldRetryNextPage: Boolean
    get() = if (isFirstPage) {
      firstPageStateD.value!! is Failure
    } else {
      loadingStateD.value!! is Failure
    }

  //endregion

  //region Public LiveDatas

  val userLiveData: LiveData<List<User>> get() = usersD

  val firstPageStateLiveData: LiveData<PlaceholderState> = firstPageStateD

  val loadingStateLiveData: LiveData<List<PlaceholderState>>
    get() = loadingStateD.map { if (it == Idle) emptyList() else listOf(it) }

  //endregion

  //region Public methods
  @MainThread
  fun loadNextPage() {
    if (shouldLoadNextPage) {
      loadNextPageInternal()
    }
  }

  @MainThread
  fun retryNextPage() {
    if (shouldRetryNextPage) {
      loadNextPageInternal()
    }
  }
  //endregion

  private fun loadNextPageInternal() {
    viewModelScope.launch {
      if (isFirstPage) {
        firstPageStateD.value = Loading
      } else {
        loadingStateD.value = Loading
      }

      val currentList = usersD.value!!

      runCatching { getUsers(currentList.size, LIMIT) }
        .fold(
          onSuccess = {
            isFirstPage = currentList.isEmpty()
            usersD.value = currentList + it

            if (isFirstPage) {
              firstPageStateD.value = Idle
            } else {
              loadingStateD.value = Idle
            }
          },
          onFailure = {
            if (isFirstPage) {
              firstPageStateD.value = Failure(it)
            } else {
              loadingStateD.value = Failure(it)
            }
          }
        )
    }
  }

  private companion object {
    const val LIMIT = 20
  }
}