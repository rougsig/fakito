package com.github.rougsig.fakito.processor.generator

import com.github.rougsig.fakito.processor.extension.*
import com.github.rougsig.fakito.runtime.Fakito
import com.squareup.kotlinpoet.*
import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import me.eugeniomarletti.kotlin.metadata.kotlinMetadata
import me.eugeniomarletti.kotlin.processing.KotlinProcessingEnvironment
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

object FakitoGenerator : Generator<FakitoGenerator.Params> {
  data class Params(
    val packageName: String,
    val fileName: String,
    val targetElement: TypeElement
  ) {
    companion object {
      fun get(env: KotlinProcessingEnvironment, targetElement: TypeElement): Params {
        val className = targetElement.className
        return Params(
          packageName = className.packageName,
          fileName = "${className.simpleName}Generated",
          targetElement = unwrapTarget(env, targetElement)
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
    val packageName = params.packageName
    val fileName = params.fileName
    val targetElement = params.targetElement

    val metadata = (targetElement.kotlinMetadata as? KotlinClassMetadata)!!.data
    val nameResolver = metadata.nameResolver
    val proto = metadata.classProto

    val methods = proto.functionList
      .map { func ->
        nameResolver.getString(func.name) to func.valueParameterList.map { nameResolver.getString(it.name) }
      }
      .toMap()

    return FileSpec.get(packageName,
      TypeSpec
        .classBuilder(fileName)
        .addModifiers(KModifier.OPEN)
        .addType(TypeSpec
          .classBuilder("Method")
          .addModifiers(KModifier.SEALED)
          .addTypes(
            targetElement
              .enclosedElements
              .mapNotNull { it as? ExecutableElement }
              .map { exec ->
                if (exec.parameters.isEmpty()) {
                  TypeSpec
                    .objectBuilder(exec.simpleName.toString().beginWithUpperCase())
                    .superclass(ClassName.bestGuess("Method"))
                    .build()
                } else {
                  val paramNames = methods.getValue(exec.simpleName.toString())

                  TypeSpec
                    .classBuilder(exec.simpleName.toString().beginWithUpperCase())
                    .addModifiers(KModifier.DATA)
                    .apply {
                      this
                        .primaryConstructor(FunSpec
                          .constructorBuilder()
                          .addParameters(exec.parameters.mapIndexed { i, param ->
                            ParameterSpec
                              .builder(paramNames[i], param.asType().asTypeName().javaToKotlinType())
                              .build()
                          })
                          .build())
                        .addProperties(exec.parameters.mapIndexed { i, param ->
                          PropertySpec
                            .builder(paramNames[i], param.asType().asTypeName().javaToKotlinType())
                            .initializer(paramNames[i])
                            .build()
                        })
                    }
                    .superclass(ClassName.bestGuess("Method"))
                    .build()
                }
              }
          )
          .build())
        .build()
    )
  }
}
