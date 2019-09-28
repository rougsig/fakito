package com.github.rougsig.fakito.processor.extension

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import kotlin.reflect.KClass

fun TypeElement.getAnnotationMirror(annotationClass: KClass<*>): AnnotationMirror? {
  return annotationMirrors
    .find { it.annotationType.asElement().simpleName.toString() == annotationClass.simpleName.toString() }
}

fun AnnotationMirror.getFieldByName(fieldName: String): AnnotationValue? {
  return elementValues.entries
    .firstOrNull { (element, _) -> element.simpleName.toString() == fieldName }
    ?.value
}

fun TypeMirror.asTypeElement(env: ProcessingEnvironment): TypeElement {
  return ((env.typeUtils
    .asElement(this)
    .asType() as DeclaredType)
    .asElement() as TypeElement)
}
