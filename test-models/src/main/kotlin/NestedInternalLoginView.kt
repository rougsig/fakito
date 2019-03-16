package com.example.mvi

import com.example.mvi.entities.FormFieldValues
import io.reactivex.Observable

internal interface Screen {
  interface NestedInternalLoginView : MviView<FormFieldValues> {
    fun navigateBackIntent(): Observable<Unit>

    fun callSupportIntent(): Observable<Unit>

    fun startLoginIntent(): Observable<FormFieldValues>

    fun otpCodeChangeIntent(): Observable<CharSequence>
  }
}
