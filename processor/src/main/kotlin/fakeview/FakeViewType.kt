package com.github.rougsig.mvifake.processor.fakeview

import com.github.rougsig.mvifake.processor.extensions.*
import com.github.rougsig.mvifake.runtime.FakeView
import me.eugeniomarletti.kotlin.processing.KotlinProcessingEnvironment
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

internal data class FakeViewType(
  val intents: List<IntentType>,
  val isInternal: Boolean,
  val viewElement: TypeElement,
  val viewName: String,
  val packageName: String
) {

  companion object {
    fun get(env: KotlinProcessingEnvironment, targetElement: TypeElement): FakeViewType? {
      val annotation = targetElement.getAnnotationMirror(FakeView::class)
      val annotationMirror = annotation!!.getFieldByName("viewClass")!!.value as TypeMirror
      val viewElement = annotationMirror.asTypeElement(env)

      val intents = viewElement.enclosedElements.mapNotNull { IntentType.get(env, it) }
      val isInternal = viewElement.isInternal

      return FakeViewType(
        intents,
        isInternal,
        viewElement,
        targetElement.className.simpleName,
        targetElement.className.packageName
      )
    }
  }
}
