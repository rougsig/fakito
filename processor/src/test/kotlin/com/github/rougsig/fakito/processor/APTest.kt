package com.github.rougsig.fakito.processor

import com.google.common.collect.ImmutableList
import com.google.common.io.Files
import com.google.testing.compile.CompilationSubject
import com.google.testing.compile.Compiler
import com.google.testing.compile.JavaFileObjects
import org.assertj.core.api.Assertions
import java.io.File
import java.nio.file.Path
import javax.annotation.processing.Processor

abstract class APTest : Assertions() {

  class MemoFile(
    val name: String,
    vararg val lines: String
  )

  fun testProcessor(
    processor: Processor,
    sourceJava: List<MemoFile>,
    expectedKotlin: List<MemoFile>,
    generationDir: File = Files.createTempDir()
  ) {
    val compilation = Compiler.javac()
      .withProcessors(processor)
      .withOptions(ImmutableList.of("-Akapt.kotlin.generated=$generationDir", "-proc:only"))
      .compile(sourceJava.map { source ->
        JavaFileObjects.forSourceLines(source.name, source.lines.map(String::trimIndent))
      })

    CompilationSubject
      .assertThat(compilation)
      .succeeded()

    generationDir.mkdirs()

    expectedKotlin.forEach { expected ->
      val nameSegments = expected.name.split(".")
      val pathSegments = nameSegments.dropLast(1)
      val fileDirectory = pathSegments.fold(generationDir) { acc, path -> File(acc, path) }
      val actualFile = File(fileDirectory, "${nameSegments.last()}.kt")

      assertThat(actualFile.readText().trimIndent())
        .isEqualTo(expected.lines.joinToString("\n").trimIndent())
    }
  }
}
