package com.github.rougsig.mvifake.processor.fakedispatchprops

import com.github.rougsig.mvifake.processor.base.UNIT_CLASS_NAME
import com.github.rougsig.mvifake.processor.extensions.asTypeElement
import com.github.rougsig.mvifake.processor.extensions.beginWithUpperCase
import com.github.rougsig.mvifake.processor.extensions.className
import com.github.rougsig.mvifake.processor.extensions.javaToKotlinType
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.VoidType
import me.eugeniomarletti.kotlin.processing.KotlinProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType

internal data class MethodType(
  val methodName: String,
  val methodElement: ExecutableElement,
  val params: List<VariableElement>
) {
  data class Param(val name: String, val type: TypeName)

  companion object {
    fun get(env: KotlinProcessingEnvironment, element: Element): MethodType? {
      val method = element as? ExecutableElement ?: return null
      val returnType = method.returnType.asTypeName()

      val isReturnTypeUnit = returnType.toString() == UNIT_CLASS_NAME.canonicalName

      if (!isReturnTypeUnit) return null
      val methodName = method.simpleName.toString().beginWithUpperCase()

      return MethodType(
        methodName,
        method,
        method.parameters
      )
    }
  }
}
