package com.github.rougsig.fakito.processor

import com.github.rougsig.fakito.processor.generator.Generator
import com.squareup.kotlinpoet.FileSpec
import java.util.*

internal class GeneratorWrapper<T>(
  private val actual: Generator<T>
) : Generator<T> {
  val generatedFiles: MutableList<FileSpec> = LinkedList<FileSpec>()

  override fun generateFile(params: T): FileSpec {
    val generatedFile = actual.generateFile(params)
    generatedFiles.add(generatedFile)
    return generatedFile
  }
}
