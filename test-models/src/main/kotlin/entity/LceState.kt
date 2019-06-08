package com.example.mvi.entity

data class LceState<T>(
  val content: T?,
  val error: Throwable?
) {
  val isLoading: Boolean
    get() = content == null && error == null
}
