package com.github.rougsig.mvifake.processor.extensions

import me.eugeniomarletti.kotlin.processing.KotlinProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

internal fun KotlinProcessingEnvironment.note(message: String, element: Element) {
  messager.printMessage(Diagnostic.Kind.NOTE, message, element)
}

internal fun KotlinProcessingEnvironment.warning(message: String, element: Element) {
  messager.printMessage(Diagnostic.Kind.WARNING, message, element)
}

internal fun KotlinProcessingEnvironment.error(message: String, element: Element) {
  messager.printMessage(Diagnostic.Kind.ERROR, message, element)
}

