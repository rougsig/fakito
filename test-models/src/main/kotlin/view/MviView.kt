package com.example.mvi.view

interface MviView<T> {
  fun render(viewState: T)
}
