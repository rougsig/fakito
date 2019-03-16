package com.example.mvi

import com.example.mvi.entities.FormFieldValues
import com.github.rougsig.mviautomock.runtime.MockView
import io.reactivex.Observable

interface LoginView : MviView<FormFieldValues> {
  fun navigateBackIntent(): Observable<Unit>

  fun callSupportIntent(): Observable<Unit>

  fun startLoginIntent(): Observable<FormFieldValues>

  fun otpCodeChangeIntent(): Observable<CharSequence>
}
