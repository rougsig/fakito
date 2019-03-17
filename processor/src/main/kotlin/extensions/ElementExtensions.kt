package com.github.rougsig.mvifake.processor.extensions

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import me.eugeniomarletti.kotlin.metadata.shadow.name.FqName
import me.eugeniomarletti.kotlin.metadata.shadow.platform.JavaToKotlinClassMap

internal fun TypeName.javaToKotlinType(): TypeName = if (this is ParameterizedTypeName) {
  (rawType.javaToKotlinType() as ClassName).parameterizedBy(
    *typeArguments.map { it.javaToKotlinType() }.toTypedArray()
  )
} else {
  val className = JavaToKotlinClassMap.mapJavaToKotlin(FqName(toString()))?.asSingleFqName()?.asString()
  if (className == null) this
  else ClassName.bestGuess(className)
}
