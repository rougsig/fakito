package com.github.rougsig.mvifake.processor.fakestateprops

import com.github.rougsig.mvifake.processor.extensions.*
import com.github.rougsig.mvifake.runtime.FakeStateProps
import me.eugeniomarletti.kotlin.processing.KotlinProcessingEnvironment
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

internal data class FakeStatePropsType(
  val props: List<PropType>,
  val isInternal: Boolean,
  val statePropsElement: TypeElement,
  val statePropsName: String,
  val packageName: String
) {
  companion object {
    fun get(env: KotlinProcessingEnvironment, targetElement: TypeElement): FakeStatePropsType? {
      val annotation = targetElement.getAnnotationMirror(FakeStateProps::class)
      val annotationMirror = annotation!!.getFieldByName("statePropsClass")!!.value as TypeMirror
      val statePropsElement = annotationMirror.asTypeElement(env)

      val props = statePropsElement.enclosedElements.mapNotNull { PropType.get(env, it) }
      val isInternal = statePropsElement.isInternal

      return FakeStatePropsType(
        props,
        isInternal,
        statePropsElement,
        targetElement.className.simpleName,
        targetElement.className.packageName
      )
    }
  }
}
