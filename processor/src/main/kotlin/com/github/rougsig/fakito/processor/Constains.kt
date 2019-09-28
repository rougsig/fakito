package com.github.rougsig.fakito.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName

internal val RELAY_CLASS_NAME = ClassName.bestGuess("com.jakewharton.rxrelay2.Relay")
internal val PUBLISH_RELAY_CLASS_NAME = ClassName.bestGuess("com.jakewharton.rxrelay2.PublishRelay")
internal val OBSERVABLE_CLASS_NAME = ClassName.bestGuess("io.reactivex.Observable")
internal val UNIT_CLASS_NAME = Unit::class.asClassName()

internal fun createParameterizedRelayType(type: TypeName): TypeName {
  return RELAY_CLASS_NAME.parameterizedBy(type)
}

internal fun createParameterizedObservableType(type: TypeName): TypeName {
  return OBSERVABLE_CLASS_NAME.parameterizedBy(type)
}
