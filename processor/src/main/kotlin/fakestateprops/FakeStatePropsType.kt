package com.github.rougsig.mvifake.processor.fakestateprops

import com.github.rougsig.mvifake.processor.extensions.*
import com.github.rougsig.mvifake.runtime.FakeStateProps
import com.github.rougsig.mvifake.runtime.FakeView
import me.eugeniomarletti.kotlin.processing.KotlinProcessingEnvironment
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

internal data class FakeStatePropsType(
  val stateProps: List<StatePropType>,
  val isInternal: Boolean,
  val viewElement: TypeElement,
  val viewName: String,
  val packageName: String
) {

  companion object {
    fun get(env: KotlinProcessingEnvironment, targetElement: TypeElement): FakeStatePropsType? {
      val annotation = targetElement.getAnnotationMirror(FakeStateProps::class)
      val annotationMirror = annotation!!.getFieldByName("statePropsClass")!!.value as TypeMirror
      val viewElement = annotationMirror.asTypeElement(env)

      val intents = viewElement.enclosedElements.mapNotNull { StatePropType.get(env, it) }
      val isInternal = viewElement.isInternal

      return FakeStatePropsType(
        intents,
        isInternal,
        viewElement,
        targetElement.className.simpleName,
        targetElement.className.packageName
      )
    }
  }
}
