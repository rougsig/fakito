package com.github.rougsig.mvifake.processor.fakedispatchprops

import com.github.rougsig.mvifake.processor.base.UNIT_CLASS_NAME
import com.github.rougsig.mvifake.processor.extension.getClassName
import com.github.rougsig.mvifake.processor.extensions.beginWithUpperCase
import com.squareup.kotlinpoet.TypeName
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import me.eugeniomarletti.kotlin.processing.KotlinProcessingEnvironment

internal data class MethodType(
  val methodName: String,
  val methodDataClassName: String,
  val params: List<Param>
) {
  data class Param(val name: String, val type: TypeName)

  companion object {
    fun get(env: KotlinProcessingEnvironment, element: ProtoBuf.Function, nameResolver: NameResolver): MethodType? {
      val returnType = nameResolver.getClassName(element.returnType.className)
      val isReturnTypeUnit = returnType.toString() == UNIT_CLASS_NAME.canonicalName
      if (!isReturnTypeUnit) return null

      val methodName = nameResolver.getString(element.name)
      val params = element.valueParameterList.map { param ->
        Param(
          nameResolver.getString(param.name),
          nameResolver.getClassName(param.type.className)
        )
      }

      return MethodType(
        methodName,
        methodName.beginWithUpperCase(),
        params
      )
    }
  }
}
