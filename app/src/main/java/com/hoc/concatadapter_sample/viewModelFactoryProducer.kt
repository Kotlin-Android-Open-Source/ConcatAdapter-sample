package com.hoc.concatadapter_sample

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

val viewModelFactoryProducer: () -> ViewModelProvider.Factory = {
  object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass == MainVM::class.java) {
        return MainVM(::getUsers) as T
      }
      error("Unknown modelClass: $modelClass")
    }
  }
}