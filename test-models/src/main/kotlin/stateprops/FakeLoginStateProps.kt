package com.example.mvi.stateprops

import com.github.rougsig.mvifake.runtime.FakeStateProps

@FakeStateProps(LoginStateProps::class)
abstract class FakeLoginStateProps

@FakeStateProps(InternalLoginStateProps::class)
abstract class FakeInternalLoginStateProps

@FakeStateProps(Screen.NestedInternalLoginStateProps::class)
abstract class FakeNestedInternalLoginStateProps
