package com.github.rougsig.mvifake.processor

import com.github.rougsig.mvifake.processor.base.Generator
import com.github.rougsig.mvifake.processor.viewgenerator.FakeViewType
import com.github.rougsig.mvifake.processor.viewgenerator.fakeViewGenerator
import com.github.rougsig.mvifake.runtime.FakeView
import com.google.auto.service.AutoService
import me.eugeniomarletti.kotlin.processing.KotlinAbstractProcessor
import java.io.File
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

private const val OPTION_GENERATED = "mvifake.generated"

@AutoService(Processor::class)
class MviFakeProcessor : KotlinAbstractProcessor() {
  private val fakeViewAnnotation = FakeView::class.java

  override fun getSupportedAnnotationTypes() = setOf(fakeViewAnnotation.canonicalName)

  override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

  override fun getSupportedOptions() = setOf(OPTION_GENERATED)

  override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
    for (type in roundEnv.getElementsAnnotatedWith(fakeViewAnnotation)) {
      val fakeViewType = FakeViewType.get(this, type) ?: continue
      fakeViewGenerator.generateAndWrite(fakeViewType)
    }
    return true
  }

  private fun <T> Generator<T>.generateAndWrite(type: T) {
    val fileSpec = generateFile(type)

    val outputDirPath = "$generatedDir/${fileSpec.packageName.replace(".", "/")}"
    val outputDir = File(outputDirPath).also { it.mkdirs() }

    val file = File(outputDir, "${fileSpec.name}.kt")
    file.writeText(fileSpec.toString())
  }
}
