package com.github.rougsig.mvifake.processor

import com.github.rougsig.mvifake.processor.base.Generator
import com.github.rougsig.mvifake.processor.fakestateprops.FakeStatePropsType
import com.github.rougsig.mvifake.processor.fakestateprops.fakeStatePropsGenerator
import com.github.rougsig.mvifake.processor.fakeview.FakeViewType
import com.github.rougsig.mvifake.processor.fakeview.fakeViewGenerator
import com.github.rougsig.mvifake.runtime.FakeDispatchProps
import com.github.rougsig.mvifake.runtime.FakeStateProps
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
  private val fakeStatePropsAnnotation = FakeStateProps::class.java
  private val fakeDispatchPropsAnnotation = FakeDispatchProps::class.java

  override fun getSupportedAnnotationTypes() = setOf(
    fakeViewAnnotation.canonicalName,
    fakeStatePropsAnnotation.canonicalName,
    fakeDispatchPropsAnnotation.canonicalName
  )

  override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

  override fun getSupportedOptions() = setOf(OPTION_GENERATED)

  override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
    processFakeViewAnnotations(annotations, roundEnv)
    processFakeStatePropsAnnotations(annotations, roundEnv)
    return true
  }

  private fun processFakeViewAnnotations(annotations: Set<TypeElement>, roundEnv: RoundEnvironment) {
    for (type in roundEnv.getElementsAnnotatedWith(fakeViewAnnotation)) {
      val targetElement = type as? TypeElement ?: continue
      val fakeViewType = FakeViewType.get(this, targetElement) ?: continue
      fakeViewGenerator.generateAndWrite(fakeViewType)
    }
  }

  private fun processFakeStatePropsAnnotations(annotations: Set<TypeElement>, roundEnv: RoundEnvironment) {
    for (type in roundEnv.getElementsAnnotatedWith(fakeStatePropsAnnotation)) {
      val targetElement = type as? TypeElement ?: continue
      val fakeViewType = FakeStatePropsType.get(this, targetElement) ?: continue
      fakeStatePropsGenerator.generateAndWrite(fakeViewType)
    }
  }

  private fun <T> Generator<T>.generateAndWrite(type: T) {
    val fileSpec = generateFile(type)

    val outputDirPath = "$generatedDir/${fileSpec.packageName.replace(".", "/")}"
    val outputDir = File(outputDirPath).also { it.mkdirs() }

    val file = File(outputDir, "${fileSpec.name}.kt")
    file.writeText(fileSpec.toString())
  }
}
