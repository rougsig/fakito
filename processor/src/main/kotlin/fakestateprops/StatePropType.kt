package com.github.rougsig.mvifake.processor.fakestateprops

import com.github.rougsig.mvifake.processor.extensions.javaToKotlinType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import me.eugeniomarletti.kotlin.processing.KotlinProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType

internal class StatePropType(
  val intentName: String,
  val valueType: TypeName
) {
  companion object {
    private val rxObservableClassName = ClassName.bestGuess("io.reactivex.Observable")

    fun get(env: KotlinProcessingEnvironment, element: Element): StatePropType? {
      val method = element as? ExecutableElement ?: return null
      val returnType = method.returnType as? DeclaredType ?: return null

      val returnTypeQualifiedName = (returnType.asElement() as TypeElement).qualifiedName
      val isReturnTypeRxObservable = returnTypeQualifiedName.toString() == rxObservableClassName.canonicalName

      if (!isReturnTypeRxObservable) return null
      val intentName = method.simpleName.toString()
      val valueType = returnType.typeArguments.first().asTypeName()

      return StatePropType(
        intentName,
        valueType.javaToKotlinType()
      )
    }
  }
}
