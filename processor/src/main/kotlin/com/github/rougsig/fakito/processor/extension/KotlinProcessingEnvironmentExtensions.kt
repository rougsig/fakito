package com.github.rougsig.fakito.processor.extension

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

internal fun ProcessingEnvironment.note(message: String, element: Element) {
  messager.printMessage(Diagnostic.Kind.NOTE, message, element)
}

internal fun ProcessingEnvironment.warning(message: String, element: Element) {
  messager.printMessage(Diagnostic.Kind.WARNING, message, element)
}

internal fun ProcessingEnvironment.error(message: String, element: Element) {
  messager.printMessage(Diagnostic.Kind.ERROR, message, element)
}

