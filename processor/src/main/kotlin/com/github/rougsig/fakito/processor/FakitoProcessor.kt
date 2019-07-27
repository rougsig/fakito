package com.github.rougsig.fakito.processor

import com.github.rougsig.fakito.processor.generator.FakitoGenerator
import com.github.rougsig.fakito.processor.generator.Generator
import com.github.rougsig.fakito.runtime.Fakito
import com.google.auto.service.AutoService
import me.eugeniomarletti.kotlin.processing.KotlinAbstractProcessor
import java.io.File
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@AutoService(Processor::class)
class FakitoProcessor : KotlinAbstractProcessor() {
  private val fakitoAnnotation = Fakito::class.java

  internal var fakitoGenerator: Generator<FakitoGenerator.Params> = FakitoGenerator

  override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
    for (type in roundEnv.getElementsAnnotatedWith(fakitoAnnotation)) {
      val targetElement = type as? TypeElement ?: continue
      val params = FakitoGenerator.Params.get(this, targetElement)
      fakitoGenerator.generateAndWrite(params)
    }
    return true
  }

  private fun <T> Generator<T>.generateAndWrite(params: T) {
    val fileSpec = generateFile(params)

    val outputDirPath = "$generatedDir/${fileSpec.packageName.replace(".", "/")}"
    val outputDir = File(outputDirPath).also { it.mkdirs() }

    val file = File(outputDir, "${fileSpec.name}.kt")
    file.writeText(fileSpec.toString())
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
