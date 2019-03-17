package com.github.rougsig.mvifake.runtime

import kotlin.reflect.KClass

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class FakeView(
  val viewClass: KClass<*>
)
