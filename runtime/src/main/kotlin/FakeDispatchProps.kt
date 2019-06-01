package com.github.rougsig.mvifake.runtime

import kotlin.reflect.KClass

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class FakeDispatchProps(
  val dispatchPropsClass: KClass<*>
)
