package com.github.rougsig.mvifake.processor.fakedispatchprops

import com.github.rougsig.mvifake.processor.extensions.*
import com.github.rougsig.mvifake.runtime.FakeDispatchProps
import me.eugeniomarletti.kotlin.processing.KotlinProcessingEnvironment
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

internal data class FakeDispatchPropsType(
  val methods: List<MethodType>,
  val isInternal: Boolean,
  val dispatchPropsElement: TypeElement,
  val dispatchPropsName: String,
  val packageName: String
) {
  companion object {
    fun get(env: KotlinProcessingEnvironment, targetElement: TypeElement): FakeDispatchPropsType? {
      val annotation = targetElement.getAnnotationMirror(FakeDispatchProps::class)
      val annotationMirror = annotation!!.getFieldByName("dispatchPropsClass")!!.value as TypeMirror
      val dispatchPropsElement = annotationMirror.asTypeElement(env)

      val methods = dispatchPropsElement.enclosedElements.mapNotNull { MethodType.get(env, it) }
      val isInternal = dispatchPropsElement.isInternal

      return FakeDispatchPropsType(
        methods,
        isInternal,
        dispatchPropsElement,
        targetElement.className.simpleName,
        targetElement.className.packageName
      )
    }
  }
}
