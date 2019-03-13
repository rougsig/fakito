package com.github.rougsig.mviautomock.annotations

import kotlin.reflect.KClass

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class MockView(
  val consumer: KClass<*>
)
