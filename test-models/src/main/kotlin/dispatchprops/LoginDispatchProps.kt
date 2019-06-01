package com.example.mvi.dispatchprops

import com.example.mvi.entities.FormFieldValues

interface LoginDispatchProps {
  fun startLogin(credentials: FormFieldValues)

  fun callToSupport()
}

internal interface InternalLoginDispatchProps {
  fun startLogin(credentials: FormFieldValues)

  fun callToSupport()
}

internal interface Screen {
  interface NestedInternalLoginDispatchProps {
    fun startLogin(credentials: FormFieldValues)

    fun callToSupport()
  }
}
