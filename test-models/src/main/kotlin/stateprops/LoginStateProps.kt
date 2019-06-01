package com.example.mvi.stateprops

import com.example.mvi.entities.LceState
import io.reactivex.Observable

interface LoginStateProps {
  fun loginState(): Observable<LceState<Unit>>

  fun login(): Observable<Unit>
}

internal interface InternalLoginStateProps {
  fun loginState(): Observable<LceState<Unit>>

  fun login(): Observable<Unit>
}

internal interface Screen {
  interface NestedInternalLoginStateProps {
    fun loginState(): Observable<LceState<Unit>>

    fun login(): Observable<Unit>
  }
}
