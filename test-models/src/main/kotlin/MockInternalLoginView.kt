package com.example.mvi

import com.example.mvi.entities.FormFieldValues
import com.github.rougsig.mviautomock.runtime.MockView

@MockView(InternalLoginView::class)
abstract class MockInternalLoginView : InternalLoginView, MviView<FormFieldValues> by StateChangeStorage()
