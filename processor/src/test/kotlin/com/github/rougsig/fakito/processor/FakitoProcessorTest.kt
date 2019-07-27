package com.github.rougsig.fakito.processor

import com.github.rougsig.fakito.processor.generator.FakitoGenerator
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import org.testng.annotations.Test

internal class FakitoProcessorTest : APTest("com.github.rougsig.fakito.processor.testdata") {
  @Test
  fun `source without methods should generate implementation without methods`() {
    val generatedFiles = runProcessor("empty.CatRepository", "empty.CatRepositoryFake")

    assertThat(generatedFiles.size).isEqualTo(1)
    val generatedFile = generatedFiles.first()
    assertThat(generatedFile.toString())
      .isEqualToIgnoringWhitespace(
        """
          package com.github.rougsig.fakito.processor.testdata.empty
          
          open class CatRepositoryFakeGenerated {
            sealed class Method
          }
        """
      )
  }

  @Test
  fun `all file methods should be declarated in Method sealed class`() {
    val generatedFiles = runProcessor("methods.CatRepository", "methods.CatRepositoryFake")

    assertThat(generatedFiles.size).isEqualTo(1)
    val generatedFile = generatedFiles.first()
    val methodSealedClass = (generatedFile.members.first() as TypeSpec).typeSpecs.find { it.name == "Method" }
    assertThat(methodSealedClass.toString())
      .isEqualToIgnoringWhitespace(
        """
          sealed class Method {
            object FetchCats : Method()
        
            data class FetchCatById(val catId: kotlin.String) : Method()
        
            object Cats : Method()
        
            data class CatById(val catId: kotlin.String) : Method()
        
            data class DeleteCats(val catIds: kotlin.collections.Set<kotlin.String>) : Method()

            data class UpdateCat(
                val catId: kotlin.String,
                val newName: kotlin.Any,
                val newHomes: kotlin.collections.List<kotlin.String>
            ) : Method()
          }
        """
      )
  }

  private fun runProcessor(vararg sources: String): List<FileSpec> {
    val processor = FakitoProcessor()
    val generatorWrapper = GeneratorWrapper(FakitoGenerator)

    processor.fakitoGenerator = generatorWrapper
    runProcessor(processor, sources.toList())

    return generatorWrapper.generatedFiles
  }
}
