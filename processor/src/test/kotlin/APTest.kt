package com.github.rougsig.mviautomock.processor

import com.google.common.collect.ImmutableList
import com.google.common.io.Files
import com.google.testing.compile.CompilationSubject
import com.google.testing.compile.Compiler
import com.google.testing.compile.JavaFileObjects
import junit.framework.TestCase
import java.io.File
import java.nio.file.Paths

abstract class APTest(
  private val pckg: String,
  private val enforcePackage: Boolean = true
) : TestCase() {
  fun testProcessor(
    vararg processor: AnnotationProcessor,
    generationDir: File = Files.createTempDir(),
    actualFileLocation: (File) -> String = { it.path }
  ) {

    processor.forEach { (name, sources, dest, proc, error) ->

      val parent = File(".").absoluteFile.parent

      val stubs = Paths.get(parent, "build", "tmp", "kapt3", "stubs", "main", *pckg.split(".").toTypedArray()).toFile()
      val expectedDir = Paths.get("", "src", "test", "resources", *pckg.split(".").toTypedArray()).toFile()

      if (dest == null && error == null) {
        throw Exception("Destination file and error cannot be both null")
      }

      if (dest != null && error != null) {
        throw Exception("Destination file or error must be set")
      }

      val compilation = Compiler.javac()
        .withProcessors(proc)
        .withOptions(ImmutableList.of("-Akapt.kotlin.generated=$generationDir", "-proc:only"))
        .compile(sources.map {
          val stub = File(stubs, it).toURI().toURL()
          JavaFileObjects.forResource(stub)
        })

      if (error != null) {

        CompilationSubject.assertThat(compilation)
          .failed()
        CompilationSubject.assertThat(compilation)
          .hadErrorContaining(error)

      } else {

        CompilationSubject.assertThat(compilation)
          .succeeded()

        val targetDir = if (enforcePackage) File("${generationDir.absolutePath}/${pckg.replace(".", "/")}") else generationDir
        assertEquals(targetDir.listFiles().size, 1)

        val expected = File(expectedDir, dest).readText()
        val actual = File(actualFileLocation(targetDir)).listFiles()[0].readText()
        assertEquals(actual.replace("\r\n", "\n"), expected.replace("\r\n", "\n"))
      }
    }
  }
}
