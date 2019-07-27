package com.github.rougsig.fakito.processor

import com.github.rougsig.fakito.processor.generator.FakitoGenerator
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import org.testng.annotations.Test

internal class FakitoProcessorTest : APTest("com.github.rougsig.fakito.processor.testdata") {
  @Test
  fun `source without methods should generate implementation without methods`() {
    val generatedFiles = runProcessor("empty.CatRepository", "empty.CatRepositoryFake")

    assertThat(generatedFiles.size).isEqualTo(1)
    val generatedFile = generatedFiles.first()
    assertThat(generatedFile)
      .isEqualTo(FileSpec
        .builder("com.github.rougsig.fakito.processor.testdata.empty", "CatRepositoryFakeGenerated")
        .addType(TypeSpec
          .classBuilder("CatRepositoryFakeGenerated")
          .addModifiers(KModifier.OPEN)
          .build())
        .build())
  }

  @Test
  fun `all file methods should be declarated in Method sealed class`() {
    val generatedFiles = runProcessor("methods.CatRepository", "methods.CatRepositoryFake")

    assertThat(generatedFiles.size).isEqualTo(1)
    val generatedFile = generatedFiles.first()
    val methodSealedClass = generatedFile.members.find { it is TypeSpec && it.name == "Method" }
    assertThat(methodSealedClass)
      .isEqualTo(TypeSpec
        .classBuilder("Method")
        .addModifiers(KModifier.SEALED)
        .build())
  }

  private fun runProcessor(vararg sources: String): List<FileSpec> {
    val processor = FakitoProcessor()
    val generatorWrapper = GeneratorWrapper(FakitoGenerator)

    processor.fakitoGenerator = generatorWrapper
    runProcessor(processor, sources.toList())

    return generatorWrapper.generatedFiles
  }
}
