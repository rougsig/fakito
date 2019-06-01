package com.example.mvi.dispatchprops

import com.github.rougsig.mvifake.runtime.FakeDispatchProps

@FakeDispatchProps(LoginDispatchProps::class)
abstract class FakeLoginDispatchProps

@FakeDispatchProps(InternalLoginDispatchProps::class)
abstract class FakeInternalLoginDispatchProps

@FakeDispatchProps(Screen.NestedInternalLoginDispatchProps::class)
abstract class FakeNestedInternalLoginDispatchProps
