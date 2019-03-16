package com.example.mvi

class StateChangeStorage<T> : MviView<T> {
  override fun render(viewState: T) = Unit
}
