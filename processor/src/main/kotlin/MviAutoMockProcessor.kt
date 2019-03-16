package com.github.rougsig.mviautomock.processor

import com.github.rougsig.mviautomock.annotations.MockView
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.asClassName
import me.eugeniomarletti.kotlin.processing.KotlinAbstractProcessor
import java.io.File
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

private const val OPTION_GENERATED = "mviautomock.generated"

@AutoService(Processor::class)
class MviAutoMockProcessor : KotlinAbstractProcessor() {
  private val annotationClass = MockView::class.java

  override fun getSupportedAnnotationTypes() = setOf(annotationClass.canonicalName)

  override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

  override fun getSupportedOptions() = setOf(OPTION_GENERATED)

  override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
    val annotatedElements = roundEnv
      .getElementsAnnotatedWith(annotationClass)
      .toSet()

    annotatedElements
      .forEachIndexed { index, el ->
        val packageName = (el as TypeElement).asClassName().packageName
        val generatedDir = File("$generatedDir/${packageName.replace(".", "/")}").also { it.mkdirs() }
        val file = File(generatedDir, "MviAutoMockProcessor.kt")
        file.writeText("class MviAutoMockProcessor {}")
      }
    return false
  }
}
