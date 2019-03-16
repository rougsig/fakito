package com.github.rougsig.mviautomock.processor

import me.eugeniomarletti.kotlin.processing.KotlinAbstractProcessor
import javax.lang.model.element.Element
import javax.tools.Diagnostic

fun KotlinAbstractProcessor.note(message: String, element: Element) {
  messager.printMessage(Diagnostic.Kind.NOTE, message, element)
}

fun KotlinAbstractProcessor.warning(message: String, element: Element) {
  messager.printMessage(Diagnostic.Kind.WARNING, message, element)
}

fun KotlinAbstractProcessor.error(message: String, element: Element) {
  messager.printMessage(Diagnostic.Kind.ERROR, message, element)
}

