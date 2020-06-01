package com.hoc.mergeadapter_sample

import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

//region Models
data class User(
  val uid: Int,
  val name: String,
  val email: String
)

object ApiError : Throwable(message = "Api error")
//endregion

//region Fake api calling
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
//endregion