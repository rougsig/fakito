package com.github.rougsig.fakito.processor.generator

import com.github.rougsig.fakito.processor.extension.*
import com.github.rougsig.fakito.runtime.Fakito
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import me.eugeniomarletti.kotlin.metadata.kotlinMetadata
import me.eugeniomarletti.kotlin.processing.KotlinProcessingEnvironment
import java.util.*
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

object FakitoGenerator : Generator<FakitoGenerator.Params> {
  data class Params(
    val packageName: String,
    val fileName: String,
    val methods: List<Method>,
    val targetElement: TypeElement
  ) {

    data class Method(
      val name: String,
      val type: TypeName,
      val params: List<Param>
    )

    data class Param(
      val name: String,
      val type: TypeName
    )

    companion object {
      fun get(env: KotlinProcessingEnvironment, targetElement: TypeElement): Params {
        val className = targetElement.className

        val fakitoTargetElement = unwrapTarget(env, targetElement)

        val metadata = (fakitoTargetElement.kotlinMetadata as? KotlinClassMetadata)!!.data
        val nameResolver = metadata.nameResolver
        val proto = metadata.classProto

        val functionProtos = proto.functionList.map { nameResolver.getString(it.name) to it }.toMap()

        val methods = fakitoTargetElement.enclosedElements
          .mapNotNull { it as? ExecutableElement }
          .map { it to functionProtos.getValue(it.simpleName.toString()) }
          .map { (exec, func) ->
            Method(
              name = nameResolver.getString(func.name),
              type = exec.returnType.asTypeName().javaToKotlinType(),
              params = exec.parameters.mapIndexed { i, param ->
                Param(
                  name = nameResolver.getString(func.valueParameterList[i].name),
                  type = param.asType().asTypeName().javaToKotlinType()
                )
              }
            )
          }

        return Params(
          packageName = className.packageName,
          fileName = "${className.simpleName}Generated",
          methods = methods,
          targetElement = fakitoTargetElement
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
        .addModifiers(KModifier.ABSTRACT)
        .addSuperinterface(params.targetElement.asClassName())
        .addMethodClass(params.methods)
        .addReturnsBuilder(params.methods)
        .addReturnsImpl(params.methods)
        .addReturnsBuilderInitFun(params.methods)
        .addImplFunctions(params.methods)
        .build()
    )
  }

  private fun TypeSpec.Builder.addMethodClass(
    methods: List<Params.Method>
  ): TypeSpec.Builder {
    if (methods.isEmpty()) return this

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
          methods
            .map { method ->
              if (method.params.isEmpty()) {
                TypeSpec
                  .objectBuilder(method.name.beginWithUpperCase())
                  .superclass(ClassName.bestGuess(methodClassName))
                  .build()
              } else {
                TypeSpec
                  .classBuilder(method.name.beginWithUpperCase())
                  .addModifiers(KModifier.DATA)
                  .primaryConstructor(FunSpec
                    .constructorBuilder()
                    .addParameters(method.params.map { param ->
                      ParameterSpec
                        .builder(param.name, param.type)
                        .build()
                    })
                    .build())
                  .addProperties(method.params.map { param ->
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
    methods: List<Params.Method>
  ): TypeSpec.Builder {
    if (methods.isEmpty()) return this

    val returnsImplClassName = "ReturnsImpl"

    return this
      .addType(TypeSpec
        .classBuilder(returnsImplClassName)
        .addModifiers(KModifier.DATA)
        .primaryConstructor(FunSpec
          .constructorBuilder()
          .addParameters(methods.map { method ->
            ParameterSpec
              .builder("${method.name}Impl", LambdaTypeName
                .get(
                  parameters = method.params.map { param ->
                    ParameterSpec
                      .builder(param.name, param.type)
                      .build()
                  },
                  returnType = method.type
                )
                .copy(nullable = true))
              .build()
          })
          .build())
        .addProperties(methods.map { method ->
          PropertySpec
            .builder("${method.name}Impl", LambdaTypeName
              .get(
                parameters = method.params.map { param ->
                  ParameterSpec
                    .builder(param.name, param.type)
                    .build()
                },
                returnType = method.type
              )
              .copy(nullable = true))
            .initializer("${method.name}Impl")
            .build()
        })
        .build())
  }

  private fun TypeSpec.Builder.addReturnsBuilder(
    methods: List<Params.Method>
  ): TypeSpec.Builder {
    if (methods.isEmpty()) return this

    val returnsBuilderClassName = "ReturnsBuilder"

    return this
      .addType(TypeSpec
        .classBuilder(returnsBuilderClassName)
        .addProperties(methods.map { method ->
          PropertySpec
            .builder("${method.name}Impl", LambdaTypeName
              .get(
                parameters = method.params.map { param ->
                  ParameterSpec
                    .builder(param.name, param.type)
                    .build()
                },
                returnType = method.type
              )
              .copy(nullable = true))
            .addModifiers(KModifier.PRIVATE)
            .mutable()
            .initializer("null")
            .build()
        })
        .addFunctions(methods.map { method ->
          val implParamName = "impl"

          FunSpec
            .builder(method.name)
            .addParameter(ParameterSpec
              .builder(implParamName, LambdaTypeName
                .get(
                  parameters = method.params.map { param ->
                    ParameterSpec
                      .builder(param.name, param.type)
                      .build()
                  },
                  returnType = method.type
                ))
              .build())
            .addStatement("this.${method.name}Impl = $implParamName")
            .addStatement("return this")
            .returns(ClassName.bestGuess(returnsBuilderClassName))
            .build()
        })
        .addFunction(FunSpec
          .builder("build")
          .returns(ClassName.bestGuess("ReturnsImpl"))
          .addCode(CodeBlock
            .builder()
            .add("return ")
            .add(CodeBlock
              .builder()
              .addStatement("ReturnsImpl(")
              .apply {
                methods.forEachIndexed { i, method ->
                  if (i == methods.lastIndex) {
                    addStatement("${method.name}Impl")
                  } else {
                    addStatement("${method.name}Impl,")
                  }
                }
              }
              .addStatement(")")
              .build())
            .build())
          .build())
        .build())
  }

  private fun TypeSpec.Builder.addReturnsBuilderInitFun(
    methods: List<Params.Method>
  ): TypeSpec.Builder {
    if (methods.isEmpty()) return this

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
    methods: List<Params.Method>
  ): TypeSpec.Builder {
    if (methods.isEmpty()) return this

    return this
      .addFunctions(methods.map { method ->
        FunSpec
          .builder(method.name)
          .addModifiers(KModifier.OVERRIDE)
          .addParameters(method.params.map { param ->
            ParameterSpec
              .builder(param.name, param.type)
              .build()
          })
          .apply {
            val params = method.params.joinToString(", ") { it.name }

            if (method.params.isEmpty()) {
              addStatement("this.methodCalls.add(Method.${method.name.beginWithUpperCase()})")
            } else {
              addStatement("this.methodCalls.add(Method.${method.name.beginWithUpperCase()}($params))")
            }

            if (method.type == Unit::class.asTypeName()) {
              addStatement("returnsImpl?.${method.name}Impl?.invoke($params)")
            } else {
              addStatement("val classImpl = this.returnsImpl ?: error(%S)",
                "returns not found for method ${method.name}($params)")
              addStatement("val methodImpl = classImpl.${method.name}Impl ?: error(%S)",
                "returns not found for method ${method.name}($params)")
              addStatement("return methodImpl.invoke($params)")
            }
          }
          .returns(method.type)
          .build()
      })
  }
}
