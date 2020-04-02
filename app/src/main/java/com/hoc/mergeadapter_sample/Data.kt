package com.hoc.mergeadapter_sample

import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

data class User(
  val uid: Int,
  val name: String,
  val email: String
)

sealed class LoadingState {
  object Loading : LoadingState()
  data class Error(val throwable: Throwable) : LoadingState()
}

object ApiError : Throwable(message = "Api error")

suspend fun getUsers(start: Int, limit: Int): List<User> {
  delay(2_000)

  if (count.getAndIncrement() == 2 && throwError.compareAndSet(true, false)) {
    throw ApiError
  }

  return List(limit) {
    User(
      uid = start + it,
      name = "Name ${start + it}",
      email = "email${start + it}@gmail.com",
    )
  }
}

private val count = AtomicInteger(0)
private val throwError = AtomicBoolean(true)