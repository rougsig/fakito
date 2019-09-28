package com.github.rougsig.fakito.processor.generator

import com.github.rougsig.fakito.processor.extension.asTypeElement
import com.github.rougsig.fakito.processor.extension.beginWithUpperCase
import com.github.rougsig.fakito.processor.extension.getAnnotationMirror
import com.github.rougsig.fakito.processor.extension.getFieldByName
import com.github.rougsig.fakito.runtime.Fakito
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.specs.toFileSpec
import com.squareup.kotlinpoet.metadata.specs.toTypeSpec
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

object FakitoGenerator : Generator<FakitoGenerator.Params> {
  data class Params(
    val fileSpec: FileSpec,
    val typeSpec: TypeSpec,
    val fakitoTarget: TypeElement,
    val funSpecs: List<FunSpec>
  ) {

    @KotlinPoetMetadataPreview
    companion object {
      fun get(env: ProcessingEnvironment, targetElement: TypeElement): Params {
        val fakitoTarget = unwrapTarget(env, targetElement)
        val typeSpec = fakitoTarget.toTypeSpec()

        return Params(
          fileSpec = targetElement.toFileSpec(),
          typeSpec = typeSpec,
          fakitoTarget = fakitoTarget,
          funSpecs = typeSpec.funSpecs
        )
      }

      private fun unwrapTarget(env: ProcessingEnvironment, targetElement: TypeElement): TypeElement {
        val annotation = targetElement.getAnnotationMirror(Fakito::class)
        val annotationMirror = annotation!!.getFieldByName("value")!!.value as TypeMirror
        return annotationMirror.asTypeElement(env)
      }
    }
  }

  override fun generateFile(params: Params): FileSpec {
    return FileSpec.get(params.fileSpec.packageName,
      TypeSpec
        .classBuilder("${params.fileSpec.name}Generated")
        .addModifiers(KModifier.ABSTRACT)
        .addSuperinterface(params.fakitoTarget.asClassName())
        .addMethodClass(params.funSpecs)
        .addReturnsBuilder(params.funSpecs)
        .addReturnsImpl(params.funSpecs)
        .addReturnsBuilderInitFun(params.funSpecs)
        .addImplFunctions(params.funSpecs)
        .build()
    )
  }

  private fun TypeSpec.Builder.addMethodClass(
    funSpecs: List<FunSpec>
  ): TypeSpec.Builder {
    if (funSpecs.isEmpty()) return this

    val methodClassName = "Method"

    return this
      .addProperty(PropertySpec
        .builder("methodCalls", LinkedList::class.asTypeName().parameterizedBy(ClassName.bestGuess(methodClassName)))
        .initializer("%T()", LinkedList::class.asTypeName())
        .build())
      .addType(TypeSpec
        .classBuilder(methodClassName)
        .addModifiers(KModifier.SEALED)
        .addTypes(
          funSpecs
            .map { funSpec ->
              if (funSpec.parameters.isEmpty()) {
                TypeSpec
                  .objectBuilder(funSpec.name.beginWithUpperCase())
                  .superclass(ClassName.bestGuess(methodClassName))
                  .build()
              } else {
                TypeSpec
                  .classBuilder(funSpec.name.beginWithUpperCase())
                  .addModifiers(KModifier.DATA)
                  .primaryConstructor(FunSpec
                    .constructorBuilder()
                    .addParameters(funSpec.parameters)
                    .build())
                  .addProperties(funSpec.parameters.map { param ->
                    PropertySpec
                      .builder(param.name, param.type)
                      .initializer(param.name)
                      .build()
                  })
                  .superclass(ClassName.bestGuess(methodClassName))
                  .build()
              }
            }
        )
        .build())
  }

  private fun TypeSpec.Builder.addReturnsImpl(
    funSpecs: List<FunSpec>
  ): TypeSpec.Builder {
    if (funSpecs.isEmpty()) return this

    val returnsImplClassName = "ReturnsImpl"

    return this
      .addType(TypeSpec
        .classBuilder(returnsImplClassName)
        .addModifiers(KModifier.DATA)
        .primaryConstructor(FunSpec
          .constructorBuilder()
          .addParameters(funSpecs.map { funSpec ->
            ParameterSpec
              .builder("${funSpec.name}Impl", LambdaTypeName
                .get(
                  parameters = funSpec.parameters,
                  returnType = funSpec.returnType ?: Unit::class.asTypeName()
                )
                .copy(nullable = true))
              .build()
          })
          .build())
        .addProperties(funSpecs.map { funSpec ->
          PropertySpec
            .builder("${funSpec.name}Impl", LambdaTypeName
              .get(
                parameters = funSpec.parameters,
                returnType = funSpec.returnType ?: Unit::class.asTypeName()
              )
              .copy(nullable = true))
            .initializer("${funSpec.name}Impl")
            .build()
        })
        .build())
  }

  private fun TypeSpec.Builder.addReturnsBuilder(
    funSpecs: List<FunSpec>
  ): TypeSpec.Builder {
    if (funSpecs.isEmpty()) return this

    val returnsBuilderClassName = "ReturnsBuilder"

    return this
      .addType(TypeSpec
        .classBuilder(returnsBuilderClassName)
        .addProperties(funSpecs.map { funSpec ->
          PropertySpec
            .builder("${funSpec.name}Impl", LambdaTypeName
              .get(
                parameters = funSpec.parameters,
                returnType = funSpec.returnType ?: Unit::class.asTypeName()
              )
              .copy(nullable = true))
            .addModifiers(KModifier.PRIVATE)
            .mutable()
            .initializer("null")
            .build()
        })
        .addFunctions(funSpecs.map { funSpec ->
          val implParamName = "impl"

          FunSpec
            .builder(funSpec.name)
            .addParameter(ParameterSpec
              .builder(implParamName, LambdaTypeName
                .get(
                  parameters = funSpec.parameters,
                  returnType = funSpec.returnType ?: Unit::class.asTypeName()
                ))
              .build())
            .addStatement("this.${funSpec.name}Impl = $implParamName")
            .addStatement("return this")
            .returns(ClassName.bestGuess(returnsBuilderClassName))
            .build()
        })
        .addFunction(FunSpec
          .builder("build")
          .returns(ClassName.bestGuess("ReturnsImpl"))
          .addStatement("""
            |return ReturnsImpl(
            |${funSpecs.joinToString(",\n") { funSpec -> "  ${funSpec.name}Impl" }}
            |)
          """.trimMargin())
          .build())
        .build())
  }

  private fun TypeSpec.Builder.addReturnsBuilderInitFun(
    funSpecs: List<FunSpec>
  ): TypeSpec.Builder {
    if (funSpecs.isEmpty()) return this

    return this
      .addProperty(PropertySpec
        .builder("returnsImpl", ClassName.bestGuess("ReturnsImpl").copy(nullable = true))
        .addModifiers(KModifier.PRIVATE)
        .mutable()
        .initializer("null")
        .build())
      .addFunction(FunSpec
        .builder("returns")
        .addParameter(ParameterSpec
          .builder("init", LambdaTypeName.get(
            receiver = ClassName.bestGuess("ReturnsBuilder"),
            returnType = Unit::class.asTypeName()
          ))
          .build()
        )
        .addStatement("val builder = ReturnsBuilder()")
        .addStatement("builder.init()")
        .addStatement("this.returnsImpl = builder.build()")
        .build())
  }

  private fun TypeSpec.Builder.addImplFunctions(
    funSpecs: List<FunSpec>
  ): TypeSpec.Builder {
    if (funSpecs.isEmpty()) return this

    return this
      .addFunctions(funSpecs.map { method ->
        FunSpec
          .builder(method.name)
          .addModifiers(KModifier.OVERRIDE)
          .addParameters(method.parameters)
          .apply {
            val params = method.parameters.joinToString(", ") { it.name }

            if (method.parameters.isEmpty()) {
              addStatement("this.methodCalls.add(Method.${method.name.beginWithUpperCase()})")
            } else {
              addStatement("this.methodCalls.add(Method.${method.name.beginWithUpperCase()}($params))")
            }

            if (method.returnType == null) {
              addStatement("returnsImpl?.${method.name}Impl?.invoke($params)")
            } else {
              addStatement("val classImpl = this.returnsImpl ?: error(%S)",
                "returns not found for method ${method.name}($params)")
              addStatement("val methodImpl = classImpl.${method.name}Impl ?: error(%S)",
                "returns not found for method ${method.name}($params)")
              addStatement("return methodImpl.invoke($params)")
            }
          }
          .returns(method.returnType ?: Unit::class.asTypeName())
          .build()
      })
  }
}
