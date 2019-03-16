package com.example.mvi

import com.example.mvi.entities.FormFieldValues
import com.github.rougsig.mviautomock.runtime.MockView

@MockView(Screen.NestedInternalLoginView::class)
abstract class MockNestedInternalLoginView : Screen.NestedInternalLoginView, MviView<FormFieldValues> by StateChangeStorage()
