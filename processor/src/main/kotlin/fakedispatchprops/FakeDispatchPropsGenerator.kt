package com.github.rougsig.mvifake.processor.fakedispatchprops

import com.github.rougsig.mvifake.processor.base.Generator
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.util.*

internal val fakeDispatchPropsGenerator: FakeDispatchPropsGenerator = FakeDispatchPropsGenerator()

internal class FakeDispatchPropsGenerator : Generator<FakeDispatchPropsType> {
  override fun generateFile(type: FakeDispatchPropsType): FileSpec {
    val className = "${type.dispatchPropsName}Generated"

    return FileSpec
      .builder(type.packageName, className)
      .addType(TypeSpec
        .classBuilder(className)
        .apply { if (type.isInternal) addModifiers(KModifier.INTERNAL) }
        .addModifiers(KModifier.ABSTRACT)
        .addSuperinterface(type.dispatchPropsElement.asClassName())
        .addMethodCallsList()
        .addMethodSealedClass(type.methods)
        .addDispatchPropsMethodImplementation(type.methods)
        .build())
      .build()
  }

  private fun TypeSpec.Builder.addMethodCallsList() = apply {
    val methodClassType = ClassName.bestGuess(METHOD_SEALED_CLASS_NAME)
    val linkedListType = LinkedList::class.asClassName().parameterizedBy(methodClassType)
    val listType = List::class.asTypeName().parameterizedBy(methodClassType)
    addProperty(PropertySpec
      .builder("_$METHOD_CALLS_LIST", linkedListType, KModifier.PRIVATE)
      .initializer("%T()", linkedListType)
      .build())
    addProperty(PropertySpec
      .builder(METHOD_CALLS_LIST, listType)
      .getter(FunSpec.getterBuilder().addStatement("return %N", "_$METHOD_CALLS_LIST").build())
      .build())
  }

  private fun TypeSpec.Builder.addMethodSealedClass(methods: List<MethodType>) = apply {
    val methodClassType = ClassName.bestGuess(METHOD_SEALED_CLASS_NAME)

    addType(TypeSpec
      .classBuilder(METHOD_SEALED_CLASS_NAME)
      .addModifiers(KModifier.SEALED)
      .addTypes(methods.map { method ->
        if (method.params.isEmpty()) {
          TypeSpec
            .objectBuilder(method.methodName)
            .superclass(methodClassType)
            .build()
        } else {
          TypeSpec
            .classBuilder(method.methodName)
            .addModifiers(KModifier.DATA)
            .primaryConstructor(FunSpec
              .constructorBuilder()
              .addParameters(method.params.map { ParameterSpec.get(it) })
              .build())
            .superclass(methodClassType)
            .build()
        }
      })
      .build())
  }

  private fun TypeSpec.Builder.addDispatchPropsMethodImplementation(methods: List<MethodType>) = apply {
    addFunctions(methods.map { method ->
      FunSpec
        .overriding(method.methodElement)
        .apply {
          if (method.params.isEmpty()) {
            addStatement("_$METHOD_CALLS_LIST.add(%N.%N)", METHOD_SEALED_CLASS_NAME, method.methodName)
          } else {
            addStatement("_$METHOD_CALLS_LIST.add(%N.%N(${method.params.joinToString { it.simpleName.toString() }}))",
              METHOD_SEALED_CLASS_NAME,
              method.methodName
            )
          }
        }
        .build()
    })
  }
}

private const val METHOD_CALLS_LIST = "methodCalls"
private const val METHOD_SEALED_CLASS_NAME = "Method"
