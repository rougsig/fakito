package com.github.rougsig.fakito.processor

import com.github.rougsig.fakito.processor.generator.FakitoGenerator
import com.github.rougsig.fakito.processor.generator.Generator
import com.github.rougsig.fakito.runtime.Fakito
import com.github.rougsig.fakito.runtime.RxFakito
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

@AutoService(Processor::class)
class FakitoProcessor : AbstractProcessor() {
  private val fakitoAnnotation = Fakito::class.java
  private val rxFakitoAnnotation = RxFakito::class.java

  private lateinit var processEnv: ProcessingEnvironment
  internal var fakitoGenerator: Generator<FakitoGenerator.Params> = FakitoGenerator

  override fun init(processingEnv: ProcessingEnvironment) {
    processEnv = processingEnv
    super.init(processingEnv)
  }

  @KotlinPoetMetadataPreview
  override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
    val elements = mutableListOf<Element>()
      .apply { addAll(roundEnv.getElementsAnnotatedWith(fakitoAnnotation)) }
      .apply { addAll(roundEnv.getElementsAnnotatedWith(rxFakitoAnnotation)) }

    elements.forEach { type ->
      (type as? TypeElement)?.let { typeElement ->
        val params = FakitoGenerator.Params.get(processEnv, typeElement)
        fakitoGenerator.generateFile(params).writeTo(processEnv.filer)
      }
    }

    return true
  }

  override fun getSupportedAnnotationTypes(): Set<String> {
    return setOf(fakitoAnnotation.canonicalName)
  }

  override fun getSupportedSourceVersion(): SourceVersion {
    return SourceVersion.latest()
  }

  override fun getSupportedOptions(): Set<String> {
    return setOf(KAPT_KOTLIN_GENERATED_OPTION_NAME)
  }
}

private const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
