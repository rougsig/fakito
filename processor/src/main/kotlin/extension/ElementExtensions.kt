package com.github.rougsig.mvifake.processor.extensions

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import me.eugeniomarletti.kotlin.metadata.kotlinMetadata
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.name.FqName
import me.eugeniomarletti.kotlin.metadata.shadow.platform.JavaToKotlinClassMap
import me.eugeniomarletti.kotlin.metadata.visibility
import me.eugeniomarletti.kotlin.processing.KotlinProcessingEnvironment
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import kotlin.reflect.KClass

internal fun TypeName.javaToKotlinType(): TypeName = if (this is ParameterizedTypeName) {
  (rawType.javaToKotlinType() as ClassName).parameterizedBy(
    *typeArguments.map { it.javaToKotlinType() }.toTypedArray()
  )
} else {
  val className = JavaToKotlinClassMap.mapJavaToKotlin(FqName(toString()))?.asSingleFqName()?.asString()
  if (className == null) this
  else ClassName.bestGuess(className)
}

val Element.className: ClassName
  get() {
    val typeName = asType().asTypeName()
    return when (typeName) {
      is ClassName -> typeName
      is ParameterizedTypeName -> typeName.rawType
      else -> throw IllegalStateException("unexpected TypeName: ${typeName::class}")
    }
  }

fun TypeElement.getAnnotationMirror(annotationClass: KClass<*>): AnnotationMirror? {
  return annotationMirrors
    .find { it.annotationType.asElement().simpleName.toString() == annotationClass.simpleName.toString() }
}

fun AnnotationMirror.getFieldByName(fieldName: String): AnnotationValue? {
  return elementValues.entries
    .firstOrNull { (element, _) -> element.simpleName.toString() == fieldName }
    ?.value
}

fun Element.getParents(list: MutableList<Element> = mutableListOf()): List<Element> {
  if (enclosingElement == null) return list.reversed()
  return enclosingElement.getParents(list.apply { add(enclosingElement) })
}

val TypeElement.isInternal: Boolean
  get() {
    return getParents()
      .plus(this)
      .filter { it.kotlinMetadata != null }
      .any { (it.kotlinMetadata as KotlinClassMetadata).data.classProto.visibility == ProtoBuf.Visibility.INTERNAL }
  }

fun TypeMirror.asTypeElement(env: KotlinProcessingEnvironment): TypeElement {
  return ((env.typeUtils
    .asElement(this)
    .asType() as DeclaredType)
    .asElement() as TypeElement)
}
