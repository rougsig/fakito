package com.example.mvi

interface MviView<T> {
  fun render(viewState: T)
}
