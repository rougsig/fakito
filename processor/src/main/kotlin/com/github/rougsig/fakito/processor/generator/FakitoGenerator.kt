package com.github.rougsig.fakito.processor.generator

import com.github.rougsig.fakito.processor.extension.asTypeElement
import com.github.rougsig.fakito.processor.extension.className
import com.github.rougsig.fakito.processor.extension.getAnnotationMirror
import com.github.rougsig.fakito.processor.extension.getFieldByName
import com.github.rougsig.fakito.runtime.Fakito
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import me.eugeniomarletti.kotlin.processing.KotlinProcessingEnvironment
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

object FakitoGenerator : Generator<FakitoGenerator.Params> {
  data class Params(
    val packageName: String,
    val fileName: String
  ) {
    companion object {
      fun get(env: KotlinProcessingEnvironment, targetElement: TypeElement): Params {
        val fakitoTargetElement = unwrapTarget(env, targetElement)

        val className = targetElement.className
        return Params(
          packageName = className.packageName,
          fileName = "${className.simpleName}Generated"
        )
      }

      private fun unwrapTarget(env: KotlinProcessingEnvironment, targetElement: TypeElement): TypeElement {
        val annotation = targetElement.getAnnotationMirror(Fakito::class)
        val annotationMirror = annotation!!.getFieldByName("value")!!.value as TypeMirror
        return annotationMirror.asTypeElement(env)
      }
    }
  }

  override fun generateFile(params: Params): FileSpec {
    return FileSpec.get(params.packageName,
      TypeSpec
        .classBuilder(params.fileName)
        .addModifiers(KModifier.OPEN)
        .build()
    )
  }
}
