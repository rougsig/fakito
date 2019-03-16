package com.example.mvi

import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import io.reactivex.Observable

abstract class LoginViewGeneratedMock : LoginView {
  protected open fun <T> createDefaultRelay(): Relay<T> {
    return PublishRelay.create()
  }

  protected open val navigateBackIntent: Relay<Unit> by lazy { createDefaultRelay<Unit>() }
  protected open val callSupportIntent: Relay<Unit> by lazy { createDefaultRelay<Unit>() }
  protected open val startLoginIntent: Relay<FormFieldValues> by lazy { createDefaultRelay<FormFieldValues>() }
  protected open val otpCodeChangeIntent: Relay<CharSequence> by lazy { createDefaultRelay<CharSequence>() }

  override fun navigateBackIntent(): Observable<Unit> {
    return navigateBackIntent
  }

  override fun callSupportIntent(): Observable<Unit> {
    return callSupportIntent
  }

  override fun startLoginIntent(): Observable<FormFieldValues> {
    return startLoginIntent
  }

  override fun otpCodeChangeIntent(): Observable<CharSequence> {
    return otpCodeChangeIntent
  }

  fun sendNavigateBackIntent() {
    navigateBackIntent.accept(Unit)
  }

  fun sendCallSupportIntent() {
    callSupportIntent.accept(Unit)
  }

  fun sendStartLoginIntent(value: FormFieldValues) {
    startLoginIntent.accept(value)
  }

  fun sendOtpCodeChangeIntent(value: CharSequence) {
    otpCodeChangeIntent.accept(value)
  }
}
