package com.example.mvi

import com.example.mvi.entities.FormFieldValues
import com.github.rougsig.mviautomock.annotations.MockView
import io.reactivex.Observable

@MockView
internal interface InternalLoginView : MviView<FormFieldValues> {
  fun navigateBackIntent(): Observable<Unit>

  fun callSupportIntent(): Observable<Unit>

  fun startLoginIntent(): Observable<FormFieldValues>

  fun otpCodeChangeIntent(): Observable<CharSequence>
}

