package com.github.rougsig.mviautomock.processor

import com.github.rougsig.mviautomock.annotations.MockView
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.asClassName
import me.eugeniomarletti.kotlin.processing.KotlinAbstractProcessor
import java.io.File
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@AutoService(Processor::class)
class MockProcessor : KotlinAbstractProcessor() {
  companion object {
    const val OPTION_GENERATED = "mviautomock.generated"
    private val POSSIBLE_GENERATED_NAMES = setOf(
      "javax.annotation.processing.Generated",
      "javax.annotation.Generated"
    )
  }

  private val annotationClass = MockView::class.java
  private lateinit var logger: Logger

  override fun getSupportedAnnotationTypes() = setOf(annotationClass.canonicalName)

  override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

  override fun getSupportedOptions() = setOf(OPTION_GENERATED)

  override fun init(processingEnv: ProcessingEnvironment) {
    super.init(processingEnv)
    logger = Logger(messager)
  }

  override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
    val annotatedElements = roundEnv
      .getElementsAnnotatedWith(annotationClass)
      .toSet()

    annotatedElements
      .forEachIndexed { index, el ->
        val packageName = (el as TypeElement).asClassName().packageName
        val generatedDir = File("$generatedDir/${packageName.replace(".", "/")}").also { it.mkdirs() }
        val file = File(generatedDir, "MockProcessor.kt")
        file.writeText("class MockProcessor {}")
      }
    return false
  }
}
