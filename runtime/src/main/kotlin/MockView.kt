package com.github.rougsig.mviautomock.runtime

import kotlin.reflect.KClass

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class MockView(
  val viewClass: KClass<*>
)
