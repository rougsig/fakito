package com.github.rougsig.mviautomock.processor

import com.github.rougsig.mviautomock.annotations.MockView
import com.google.auto.service.AutoService
import me.eugeniomarletti.kotlin.processing.KotlinAbstractProcessor
import java.io.File
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

private const val OPTION_GENERATED = "mviautomock.generated"

@AutoService(Processor::class)
class MviAutoMockProcessor : KotlinAbstractProcessor() {
  private val mockViewAnnotation = MockView::class.java

  override fun getSupportedAnnotationTypes() = setOf(mockViewAnnotation.canonicalName)

  override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

  override fun getSupportedOptions() = setOf(OPTION_GENERATED)

  override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
    for (type in roundEnv.getElementsAnnotatedWith(mockViewAnnotation)) {
      val mockViewType = MockViewType.get(this, type) ?: continue
      mockViewGenerator.generateAndWrite(mockViewType)
    }
    return true
  }

  private fun <T> Generator<T>.generateAndWrite(type: T) {
    val fileSpec = generateFile(type)

    val outputDirPath = "$generatedDir/${fileSpec.packageName.replace(".", "/")}"
    val outputDir = File(outputDirPath).also { it.mkdirs() }

    val file = File(outputDir, fileSpec.name)
    file.writeText(fileSpec.toString())
  }
}
