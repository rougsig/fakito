package com.github.rougsig.fakito.processor.extension

import com.squareup.kotlinpoet.ClassName
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver

fun NameResolver.getClassName(index: Int): ClassName {
  return ClassName.bestGuess(getQualifiedClassName(index).replace("/", "."))
}
