package com.example.mvi

import com.github.rougsig.mvifake.runtime.FakeView

@FakeView(LoginView::class)
abstract class FakeLoginView

@FakeView(InternalLoginView::class)
abstract class FakeInternalLoginView

@FakeView(Screen.NestedInternalLoginView::class)
abstract class FakeNestedInternalLoginView
