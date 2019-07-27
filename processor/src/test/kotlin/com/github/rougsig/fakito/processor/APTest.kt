package com.github.rougsig.fakito.processor

import com.google.common.collect.ImmutableList
import com.google.common.io.Files
import com.google.testing.compile.CompilationSubject
import com.google.testing.compile.Compiler
import com.google.testing.compile.JavaFileObjects
import org.assertj.core.api.Assertions
import java.io.File
import java.nio.file.Paths
import javax.annotation.processing.Processor

abstract class APTest(
  private val packageName: String
) : Assertions() {
  fun runProcessor(
    processor: Processor,
    sources: List<String>,
    generationDir: File = Files.createTempDir()
  ): File {
    val projectRoot = File(".").absoluteFile.parent
    val packageNameDir = packageName.replace(".", "/")
    val stubs = Paths.get(projectRoot, TEST_MODELS_STUB_DIR, packageNameDir).toFile()

    val compilation = Compiler.javac()
      .withProcessors(processor)
      .withOptions(ImmutableList.of("-Akapt.kotlin.generated=$generationDir", "-proc:only"))
      .compile(sources.map {
        val stub = File(stubs, it.replace(".", "/") + ".java").toURI().toURL()
        JavaFileObjects.forResource(stub)
      })

    CompilationSubject
      .assertThat(compilation)
      .succeeded()

    return generationDir
  }
}

private const val TEST_MODELS_STUB_DIR = "processor/build/tmp/kapt3/stubs/test"
