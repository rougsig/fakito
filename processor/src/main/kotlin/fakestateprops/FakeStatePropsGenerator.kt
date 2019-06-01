package com.github.rougsig.mvifake.processor.fakestateprops

import com.github.rougsig.mvifake.processor.base.*
import com.github.rougsig.mvifake.processor.base.Generator
import com.github.rougsig.mvifake.processor.base.PUBLISH_RELAY_CLASS_NAME
import com.github.rougsig.mvifake.processor.base.UNIT_CLASS_NAME
import com.github.rougsig.mvifake.processor.base.createParameterizedObservableType
import com.github.rougsig.mvifake.processor.extensions.beginWithUpperCase
import com.squareup.kotlinpoet.*

internal val fakeStatePropsGenerator: FakeStatePropsGenerator = FakeStatePropsGenerator()

internal class FakeStatePropsGenerator : Generator<FakeStatePropsType> {
  override fun generateFile(type: FakeStatePropsType): FileSpec {
    val className = "${type.statePropsName}Generated"

    return FileSpec
      .builder(type.packageName, className)
      .addType(TypeSpec
        .classBuilder(className)
        .apply { if (type.isInternal) addModifiers(KModifier.INTERNAL) }
        .addModifiers(KModifier.ABSTRACT)
        .addSuperinterface(type.statePropsElement.asClassName())
        .addCreateDefaultRelayFun()
        .addRelayProperties(type.props)
        .addSendPropFunctions(type.props)
        .addGetPropObservableFunctions(type.props)
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

  private fun TypeSpec.Builder.addRelayProperties(props: List<PropType>) = apply {
    addProperties(props.map { intent ->
      PropertySpec
        .builder(intent.intentName, createParameterizedRelayType(intent.valueType))
        .addModifiers(KModifier.PROTECTED, KModifier.OPEN)
        .delegate("lazy { %L<%T>() }", CREATE_DEFAULT_RELAY_FUN_NAME, intent.valueType)
        .build()
    })
  }

  private fun TypeSpec.Builder.addSendPropFunctions(props: List<PropType>) = apply {
    addFunctions(props.map { intent ->
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

  private fun TypeSpec.Builder.addGetPropObservableFunctions(props: List<PropType>) = apply {
    addFunctions(props.map { intent ->
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
