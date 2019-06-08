package com.example.mvi.view

import com.example.mvi.entity.FormFieldValues
import io.reactivex.Observable

interface LoginView : MviView<FormFieldValues> {
  fun navigateBackIntent(): Observable<Unit>

  fun callSupportIntent(): Observable<Unit>

  fun startLoginIntent(): Observable<FormFieldValues>

  fun otpCodeChangeIntent(): Observable<CharSequence>
}

internal interface InternalLoginView : MviView<FormFieldValues> {
  fun navigateBackIntent(): Observable<Unit>

  fun callSupportIntent(): Observable<Unit>

  fun startLoginIntent(): Observable<FormFieldValues>

  fun otpCodeChangeIntent(): Observable<CharSequence>
}

internal interface Screen {
  interface NestedInternalLoginView : MviView<FormFieldValues> {
    fun navigateBackIntent(): Observable<Unit>

    fun callSupportIntent(): Observable<Unit>

    fun startLoginIntent(): Observable<FormFieldValues>

    fun otpCodeChangeIntent(): Observable<CharSequence>
  }
}
