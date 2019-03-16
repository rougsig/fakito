package com.github.rougsig.mviautomock.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.asTypeName
import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import me.eugeniomarletti.kotlin.metadata.classKind
import me.eugeniomarletti.kotlin.metadata.kotlinMetadata
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf.Class
import me.eugeniomarletti.kotlin.metadata.visibility
import me.eugeniomarletti.kotlin.processing.KotlinProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

internal data class MockViewType(
  val proto: Class,
  val element: TypeElement,
  val intents: List<IntentType>,
  val isInternal: Boolean
) {
  val viewName = element.className
  val packageName = viewName.packageName

  companion object {
    fun get(env: KotlinProcessingEnvironment, element: Element): MockViewType? {
      val typeMetadata = element.kotlinMetadata
      if (element !is TypeElement || typeMetadata !is KotlinClassMetadata) {
        env.error("@MockView can't be applied to $element: must be kotlin interface", element)
        return null
      }

      val proto = typeMetadata.data.classProto
      if (proto.classKind != Class.Kind.INTERFACE) {
        env.error("@MockView can't be applied to $element: must be a interface", element)
        return null
      }

      val intents = element.enclosedElements.mapNotNull { IntentType.get(env, it) }
      val isInternal = proto.visibility!! == ProtoBuf.Visibility.INTERNAL

      return MockViewType(
        proto,
        element,
        intents,
        isInternal
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
