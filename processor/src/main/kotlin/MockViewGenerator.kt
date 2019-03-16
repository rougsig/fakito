package com.github.rougsig.mviautomock.processor

import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.reactivex.Observable

internal val mockViewGenerator: MockViewGenerator = MockViewGenerator()

internal class MockViewGenerator : Generator<MockViewType> {
  private val relayTypeName = Relay::class.asTypeName()
  private val observableTypeName = Observable::class.asTypeName()
  private val unitTypeName = Unit::class.asTypeName()
  private val createDefaultRelayFunName = "createDefaultRelay"

  private fun createParameterizedRelayType(type: TypeName): TypeName {
    return relayTypeName.parameterizedBy(type)
  }

  private fun createParameterizedObservableType(type: TypeName): TypeName {
    return observableTypeName.parameterizedBy(type)
  }

  override fun generateFile(type: MockViewType): FileSpec {
    val className = "${type.viewName}GeneratedMock"

    return FileSpec
      .builder(type.packageName, className)
      .addType(TypeSpec
        .classBuilder(className)
        .apply { if (type.isInternal) addModifiers(KModifier.INTERNAL) }
        .addModifiers(KModifier.ABSTRACT)
        .addSuperinterface(type.className)
        .addFunction(FunSpec
          .builder(createDefaultRelayFunName)
          .addModifiers(KModifier.PROTECTED, KModifier.OPEN)
          .addTypeVariable(TypeVariableName("T"))
          .returns(createParameterizedRelayType(TypeVariableName("T")))
          .addStatement("return %T.create<T>()", PublishRelay::class.asTypeName())
          .build())
        .addProperties(type.intents.map { intent ->
          PropertySpec
            .builder(intent.intentName, createParameterizedRelayType(intent.valueType))
            .addModifiers(KModifier.PROTECTED, KModifier.OPEN)
            .delegate("lazy { %L<%T>() }", createDefaultRelayFunName, intent.valueType)
            .build()
        })
        .addFunctions(type.intents.map { intent ->
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
        .addFunctions(type.intents.map { intent ->
          FunSpec
            .builder(intent.intentName)
            .addModifiers(KModifier.OVERRIDE)
            .returns(createParameterizedObservableType(intent.valueType))
            .addStatement("return %L", intent.intentName)
            .build()
        })
        .build())
      .build()
  }
}
