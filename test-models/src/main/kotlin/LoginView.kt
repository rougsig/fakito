package com.example.mvi

import com.github.rougsig.mviautomock.annotations.MockView
import io.reactivex.Observable

@MockView
interface LoginView: MviView<FormFieldValues> {
  fun navigateBackIntent(): Observable<Unit>

  fun callSupportIntent(): Observable<Unit>

  fun startLoginIntent(): Observable<FormFieldValues>

  fun otpCodeChangeIntent(): Observable<CharSequence>
}

interface MviView<T> {
  fun render(viewState: T)
}
