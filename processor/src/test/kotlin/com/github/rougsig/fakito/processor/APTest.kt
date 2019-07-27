package com.github.rougsig.fakito.processor

import com.google.common.collect.ImmutableList
import com.google.common.io.Files
import com.google.testing.compile.CompilationSubject
import com.google.testing.compile.Compiler
import com.google.testing.compile.JavaFileObjects
import org.assertj.core.api.Assertions
import java.io.File
import javax.annotation.processing.Processor

abstract class APTest : Assertions() {

  class MemoFile(
    val name: String,
    vararg val lines: String
  )

  fun testProcessor(
    processor: Processor,
    source: MemoFile,
    expected: MemoFile?,
    generationDir: File = Files.createTempDir()
  ) {
    val compilation = Compiler.javac()
      .withProcessors(processor)
      .withOptions(ImmutableList.of("-Akapt.kotlin.generated=$generationDir", "-proc:only"))
      .compile(JavaFileObjects.forSourceLines(source.name, *source.lines))

    CompilationSubject
      .assertThat(compilation)
      .succeeded()

    generationDir.mkdirs()
    val generatedFiles = generationDir.listFiles() ?: emptyArray()

    if (expected != null) {
      assertThat(generatedFiles.size)
        .isEqualTo(1)

      val actualFile = generatedFiles.first()

      assertThat(actualFile.name)
        .isEqualTo(expected.name)

      assertThat(actualFile.readText())
        .isEqualToIgnoringWhitespace(expected.lines.joinToString("\n"))
    } else {
      assertThat(generatedFiles.size)
        .isEqualTo(0)
    }
  }
}
