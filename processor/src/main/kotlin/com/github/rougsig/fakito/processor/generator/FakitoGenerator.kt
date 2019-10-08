package com.github.rougsig.fakito.processor.generator

import com.github.rougsig.fakito.processor.OBSERVABLE_CLASS_NAME
import com.github.rougsig.fakito.processor.PUBLISH_RELAY_CLASS_NAME
import com.github.rougsig.fakito.processor.UNIT_CLASS_NAME
import com.github.rougsig.fakito.processor.createParameterizedRelayType
import com.github.rougsig.fakito.processor.extension.asTypeElement
import com.github.rougsig.fakito.processor.extension.beginWithUpperCase
import com.github.rougsig.fakito.processor.extension.getAnnotationMirror
import com.github.rougsig.fakito.processor.extension.getFieldByName
import com.github.rougsig.fakito.runtime.Fakito
import com.github.rougsig.fakito.runtime.RxFakito
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.specs.toFileSpec
import com.squareup.kotlinpoet.metadata.specs.toTypeSpec
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

object FakitoGenerator : Generator<FakitoGenerator.Params> {
  data class Params(
    val fileSpec: FileSpec,
    val typeSpec: TypeSpec,
    val isInternal: Boolean,
    val fakitoTarget: TypeElement,
    val funSpecs: List<FunSpec>,
    val rxFunSpecs: List<FunSpec>,
    val generateRxJavaUtils: Boolean
  ) {

    @KotlinPoetMetadataPreview
    companion object {
      fun get(env: ProcessingEnvironment, targetElement: TypeElement): Params {
        val annotation = targetElement.getAnnotationMirror(Fakito::class)
        val rxAnnotation = targetElement.getAnnotationMirror(RxFakito::class)

        val isRxAnnotation = rxAnnotation != null

        val fakitoTarget = unwrapTarget(env, if (isRxAnnotation) rxAnnotation!! else annotation!!)
        val typeSpec = fakitoTarget.toTypeSpec()

        return Params(
          fileSpec = targetElement.toFileSpec(),
          typeSpec = typeSpec,
          fakitoTarget = fakitoTarget,
          isInternal = targetElement.toTypeSpec().modifiers.contains(KModifier.INTERNAL),
          funSpecs = typeSpec.funSpecs,
          generateRxJavaUtils = isRxAnnotation,
          rxFunSpecs = typeSpec.funSpecs
            .filter {
              val rawType = (it.returnType as? ParameterizedTypeName)?.rawType
              rawType?.toString() == OBSERVABLE_CLASS_NAME.toString() && it.parameters.isEmpty()
            }
        )
      }

      private fun unwrapTarget(env: ProcessingEnvironment, annotationMirror: AnnotationMirror): TypeElement {
        return (annotationMirror.getFieldByName("value")!!.value as TypeMirror).asTypeElement(env)
      }
    }
  }

  override fun generateFile(params: Params): FileSpec {
    return FileSpec.get(params.fileSpec.packageName,
      TypeSpec
        .classBuilder("${params.fileSpec.name}Generated")
        .addModifiers(KModifier.ABSTRACT)
        .apply { if (params.isInternal) addModifiers(KModifier.INTERNAL) }
        .addSuperinterface(params.fakitoTarget.asClassName())
        .apply {
          if (params.generateRxJavaUtils) {
            addCreateDefaultRelayFunction()
            addRelayProperties(params.rxFunSpecs)
            addSendIntentFunctions(params.rxFunSpecs)
          }
        }
        .addMethodClass(params.funSpecs)
        .addReturnsBuilder(params.funSpecs, if (params.generateRxJavaUtils) params.rxFunSpecs else emptyList())
        .addReturnsImpl(params.funSpecs)
        .addReturnsBuilderInitFun(params.funSpecs)
        .addImplFunctions(params.funSpecs)
        .build()
    )
  }

  private fun TypeSpec.Builder.addCreateDefaultRelayFunction(): TypeSpec.Builder {
    return this
      .addFunction(FunSpec
        .builder(CREATE_DEFAULT_RELAY_FUN_NAME)
        .addModifiers(KModifier.PROTECTED, KModifier.OPEN)
        .addTypeVariable(TypeVariableName("T"))
        .returns(createParameterizedRelayType(TypeVariableName("T")))
        .addStatement("return %T.create<T>()", PUBLISH_RELAY_CLASS_NAME)
        .build())
  }

  private fun TypeSpec.Builder.addRelayProperties(rxFunSpecs: List<FunSpec>): TypeSpec.Builder {
    return this
      .addProperties(rxFunSpecs.map { funSpec ->
        val rxType = (funSpec.returnType as ParameterizedTypeName).typeArguments.first()
        PropertySpec
          .builder("${funSpec.name}Relay", createParameterizedRelayType(rxType))
          .addModifiers(KModifier.PROTECTED, KModifier.OPEN)
          .delegate("lazy { %L<%T>() }", CREATE_DEFAULT_RELAY_FUN_NAME, rxType)
          .build()
      })
  }

  private fun TypeSpec.Builder.addSendIntentFunctions(rxFunSpecs: List<FunSpec>): TypeSpec.Builder {
    return this
      .addFunctions(rxFunSpecs.map { funSpec ->
        val rxType = (funSpec.returnType as ParameterizedTypeName).typeArguments.first()
        FunSpec
          .builder("send${funSpec.name.beginWithUpperCase()}")
          .apply {
            if (funSpec.returnType != null) {
              addParameter("value", rxType)
              addStatement("%L.accept(value)", "${funSpec.name}Relay")
            } else {
              addStatement("%L.accept(%T)", "${funSpec.name}Relay", UNIT_CLASS_NAME)
            }
          }
          .build()
      })
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
                  returnType = funSpec.returnType ?: UNIT_CLASS_NAME
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
                returnType = funSpec.returnType ?: UNIT_CLASS_NAME
              )
              .copy(nullable = true))
            .initializer("${funSpec.name}Impl")
            .build()
        })
        .build())
  }

  private fun TypeSpec.Builder.addReturnsBuilder(
    funSpecs: List<FunSpec>,
    rxFunSpecs: List<FunSpec>
  ): TypeSpec.Builder {
    if (funSpecs.isEmpty()) return this

    val returnsBuilderClassName = "ReturnsBuilder"

    return this
      .addType(TypeSpec
        .classBuilder(returnsBuilderClassName)
        .addModifiers(KModifier.INNER)
        .addProperties(funSpecs.map { funSpec ->
          PropertySpec
            .builder("${funSpec.name}Impl", LambdaTypeName
              .get(
                parameters = funSpec.parameters,
                returnType = funSpec.returnType ?: UNIT_CLASS_NAME
              )
              .copy(nullable = true))
            .addModifiers(KModifier.PRIVATE)
            .mutable()
            .apply {
              if (rxFunSpecs.contains(funSpec)) {
                initializer("{ ${funSpec.name}Relay }")
              } else {
                initializer("null")
              }
            }
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
                  returnType = funSpec.returnType ?: UNIT_CLASS_NAME
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
            returnType = UNIT_CLASS_NAME
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
          .returns(method.returnType ?: UNIT_CLASS_NAME)
          .build()
      })
  }
}

private const val CREATE_DEFAULT_RELAY_FUN_NAME = "createDefaultRelay"
