package com.github.rougsig.mviautomock.processor

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

internal val mockViewGenerator: MockViewGenerator = MockViewGenerator()

internal class MockViewGenerator : Generator<MockViewType> {

  private val unitTypeName = Unit::class.asTypeName()
  private val createDefaultRelayFunName = "createDefaultRelay"

  private fun createParameterizedRelayType(type: TypeName): TypeName {
    return relayClassName.parameterizedBy(type)
  }

  private fun createParameterizedObservableType(type: TypeName): TypeName {
    return observableClassName.parameterizedBy(type)
  }

  override fun generateFile(type: MockViewType): FileSpec {
    val className = "${type.viewName}Generated"

    return FileSpec
      .builder(type.packageName, className)
      .addType(TypeSpec
        .classBuilder(className)
        .apply { if (type.isInternal) addModifiers(KModifier.INTERNAL) }
        .addModifiers(KModifier.ABSTRACT)
        .addSuperinterface(type.viewElement.asClassName())
        .addCreateDefaultRelayFun()
        .addRelayProperties(type.intents)
        .addSendIntentFunctions(type.intents)
        .addGetIntentObservableFunctions(type.intents)
        .build())
      .build()
  }

  private fun TypeSpec.Builder.addCreateDefaultRelayFun() = apply {
    addFunction(FunSpec
      .builder(createDefaultRelayFunName)
      .addModifiers(KModifier.PROTECTED, KModifier.OPEN)
      .addTypeVariable(TypeVariableName("T"))
      .returns(createParameterizedRelayType(TypeVariableName("T")))
      .addStatement("return %T.create<T>()", publishRelayClassName)
      .build())
  }

  private fun TypeSpec.Builder.addRelayProperties(intents: List<IntentType>) = apply {
    addProperties(intents.map { intent ->
      PropertySpec
        .builder(intent.intentName, createParameterizedRelayType(intent.valueType))
        .addModifiers(KModifier.PROTECTED, KModifier.OPEN)
        .delegate("lazy { %L<%T>() }", createDefaultRelayFunName, intent.valueType)
        .build()
    })
  }

  private fun TypeSpec.Builder.addSendIntentFunctions(intents: List<IntentType>) = apply {
    addFunctions(intents.map { intent ->
      FunSpec
        .builder("send${intent.intentName.beginWithUpperCase()}")
        .apply {
          if (intent.valueType.toString() != unitTypeName.toString()) {
            addParameter("value", intent.valueType)
            addStatement("%L.accept(value)", intent.intentName)
          } else {
            addStatement("%L.accept(%T)", intent.intentName, unitTypeName)
          }
        }
        .build()
    })
  }

  private fun TypeSpec.Builder.addGetIntentObservableFunctions(intents: List<IntentType>) = apply {
    addFunctions(intents.map { intent ->
      FunSpec
        .builder(intent.intentName)
        .addModifiers(KModifier.OVERRIDE)
        .returns(createParameterizedObservableType(intent.valueType))
        .addStatement("return %L", intent.intentName)
        .build()
    })
  }
}
