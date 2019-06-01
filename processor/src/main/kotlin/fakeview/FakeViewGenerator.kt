package com.github.rougsig.mvifake.processor.fakeview

import com.github.rougsig.mvifake.processor.base.*
import com.github.rougsig.mvifake.processor.base.Generator
import com.github.rougsig.mvifake.processor.base.PUBLISH_RELAY_CLASS_NAME
import com.github.rougsig.mvifake.processor.extensions.beginWithUpperCase
import com.squareup.kotlinpoet.*

internal val fakeViewGenerator: FakeViewGenerator = FakeViewGenerator()

internal class FakeViewGenerator : Generator<FakeViewType> {
  override fun generateFile(type: FakeViewType): FileSpec {
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
      .builder(CREATE_DEFAULT_RELAY_FUN_NAME)
      .addModifiers(KModifier.PROTECTED, KModifier.OPEN)
      .addTypeVariable(TypeVariableName("T"))
      .returns(createParameterizedRelayType(TypeVariableName("T")))
      .addStatement("return %T.create<T>()", PUBLISH_RELAY_CLASS_NAME)
      .build())
  }

  private fun TypeSpec.Builder.addRelayProperties(intents: List<IntentType>) = apply {
    addProperties(intents.map { intent ->
      PropertySpec
        .builder(intent.intentName, createParameterizedRelayType(intent.valueType))
        .addModifiers(KModifier.PROTECTED, KModifier.OPEN)
        .delegate("lazy { %L<%T>() }", CREATE_DEFAULT_RELAY_FUN_NAME, intent.valueType)
        .build()
    })
  }

  private fun TypeSpec.Builder.addSendIntentFunctions(intents: List<IntentType>) = apply {
    addFunctions(intents.map { intent ->
      FunSpec
        .builder("send${intent.intentName.beginWithUpperCase()}")
        .apply {
          if (intent.valueType.toString() != UNIT_CLASS_NAME.toString()) {
            addParameter("value", intent.valueType)
            addStatement("%L.accept(value)", intent.intentName)
          } else {
            addStatement("%L.accept(%T)", intent.intentName, UNIT_CLASS_NAME)
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

private const val CREATE_DEFAULT_RELAY_FUN_NAME = "createDefaultRelay"
