package com.github.rougsig.mviautomock.processor

import com.github.rougsig.mviautomock.runtime.MockView
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.asTypeName
import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import me.eugeniomarletti.kotlin.metadata.kotlinMetadata
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.visibility
import me.eugeniomarletti.kotlin.processing.KotlinProcessingEnvironment
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import kotlin.reflect.KClass

internal data class MockViewType(
  val intents: List<IntentType>,
  val isInternal: Boolean,
  val viewElement: TypeElement,
  val viewName: String,
  val packageName: String
) {

  companion object {
    fun get(env: KotlinProcessingEnvironment, element: Element): MockViewType? {
      val typeMetadata = element.kotlinMetadata
      if (element !is TypeElement || typeMetadata !is KotlinClassMetadata) {
        env.error("@MockView can't be applied to $element: must be kotlin class", element)
        return null
      }

      val annotation = element.getAnnotationMirror(MockView::class)
      val viewClassName = annotation!!.getFieldByName("viewClass")!!.value.toString()

      val viewElement = (element.interfaces
        .find { it.asTypeName().toString() == viewClassName }!! as DeclaredType)
        .asElement() as TypeElement

      val viewElementTypeMetadata = viewElement.kotlinMetadata as? KotlinClassMetadata ?: return null

      val proto = viewElementTypeMetadata.data.classProto
      val intents = viewElement.enclosedElements.mapNotNull { IntentType.get(env, it) }
      val isInternal = proto.visibility!! == ProtoBuf.Visibility.INTERNAL

      return MockViewType(
        intents,
        isInternal,
        viewElement,
        viewElement.className.simpleName,
        element.className.packageName
      )
    }
  }
}

private val Element.className: ClassName
  get() {
    val typeName = asType().asTypeName()
    return when (typeName) {
      is ClassName -> typeName
      is ParameterizedTypeName -> typeName.rawType
      else -> throw IllegalStateException("unexpected TypeName: ${typeName::class}")
    }
  }

private fun TypeElement.getAnnotationMirror(annotationClass: KClass<*>): AnnotationMirror? {
  return annotationMirrors
    .find { it.annotationType.asElement().simpleName.toString() == annotationClass.simpleName.toString() }
}

private fun AnnotationMirror.getFieldByName(fieldName: String): AnnotationValue? {
  return elementValues.entries
    .firstOrNull { (element, _) ->
      element.simpleName.toString() == fieldName
    }
    ?.value
}
