package com.example.mvi

import com.example.mvi.entities.FormFieldValues
import com.github.rougsig.mviautomock.runtime.MockView

@MockView(LoginView::class)
abstract class MockLoginView : LoginView, MviView<FormFieldValues> by StateChangeStorage()
