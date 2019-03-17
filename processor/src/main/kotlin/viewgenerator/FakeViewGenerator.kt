package com.github.rougsig.mvifake.processor.viewgenerator

import com.github.rougsig.mvifake.processor.base.Generator
import com.github.rougsig.mvifake.processor.base.OBSERVABLE_CLASS_NAME
import com.github.rougsig.mvifake.processor.base.PUBLISH_RELAY_CLASS_NAME
import com.github.rougsig.mvifake.processor.base.RELAY_CLASS_NAME
import com.github.rougsig.mvifake.processor.extensions.beginWithUpperCase
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

internal val fakeViewGenerator: FakeViewGenerator = FakeViewGenerator()

internal class FakeViewGenerator : Generator<FakeViewType> {

  private val unitTypeName = Unit::class.asTypeName()
  private val createDefaultRelayFunName = "createDefaultRelay"

  private fun createParameterizedRelayType(type: TypeName): TypeName {
    return RELAY_CLASS_NAME.parameterizedBy(type)
  }

  private fun createParameterizedObservableType(type: TypeName): TypeName {
    return OBSERVABLE_CLASS_NAME.parameterizedBy(type)
  }

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
      .builder(createDefaultRelayFunName)
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
