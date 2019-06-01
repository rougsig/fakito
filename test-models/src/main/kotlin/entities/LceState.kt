package com.example.mvi.entities

data class LceState<T>(
  val content: T?,
  val error: Throwable?
) {
  val isLoading: Boolean
    get() = content == null && error == null
}
