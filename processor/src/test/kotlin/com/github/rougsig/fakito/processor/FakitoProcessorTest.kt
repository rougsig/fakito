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
          
          open class CatRepositoryFakeGenerated
        """.trimIndent()
      )
  }

  @Test
  fun `all methods should be declarated in Method sealed class`() {
    val generatedFiles = runProcessor("methods.CatRepository", "methods.CatRepositoryFake")

    assertThat(generatedFiles.size).isEqualTo(1)
    val generatedFile = generatedFiles.first()
    val methodClass = (generatedFile.members.first() as TypeSpec).typeSpecs.find { it.name == "Method" }
    assertThat(methodClass.toString())
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
        """.trimIndent()
      )
  }

  @Test
  fun `all methods with parameter should be declarated in ReturnsBuilder class`() {
    val generatedFiles = runProcessor("methods.CatRepository", "methods.CatRepositoryFake")

    assertThat(generatedFiles.size).isEqualTo(1)
    val generatedFile = generatedFiles.first()
    val returnsBuilderClass = (generatedFile.members.first() as TypeSpec).typeSpecs.find { it.name == "ReturnsBuilder" }
    assertThat(returnsBuilderClass.toString())
      .isEqualToIgnoringWhitespace(
        """
          class ReturnsBuilder {
            private var fetchCatsImpl: (() -> kotlin.Unit)? = null
          
            private var fetchCatByIdImpl: ((catId: kotlin.String) -> kotlin.Unit)? = null
          
            private var catsImpl: (() -> kotlin.collections.List<kotlin.Any>)? = null
          
            private var catByIdImpl: ((catId: kotlin.String) -> kotlin.Any)? = null
          
            private var deleteCatsImpl: ((catIds: kotlin.collections.Set<kotlin.String>) -> kotlin.Unit)? = null
          
            private var updateCatImpl: ((
                catId: kotlin.String,
                newName: kotlin.Any,
                newHomes: kotlin.collections.List<kotlin.String>
            ) -> kotlin.Unit)? = null
          
            fun fetchCats(impl: () -> kotlin.Unit): ReturnsBuilder {
                this.fetchCatsImpl = impl
                return this
            }
          
            fun fetchCatById(impl: (catId: kotlin.String) -> kotlin.Unit): ReturnsBuilder {
                this.fetchCatByIdImpl = impl
                return this
            }
          
            fun cats(impl: () -> kotlin.collections.List<kotlin.Any>): ReturnsBuilder {
                this.catsImpl = impl
                return this
            }
          
            fun catById(impl: (catId: kotlin.String) -> kotlin.Any): ReturnsBuilder {
                this.catByIdImpl = impl
                return this
            }
          
            fun deleteCats(impl: (catIds: kotlin.collections.Set<kotlin.String>) -> kotlin.Unit): ReturnsBuilder {
                this.deleteCatsImpl = impl
                return this
            }
          
            fun updateCat(impl: (
                catId: kotlin.String,
                newName: kotlin.Any,
                newHomes: kotlin.collections.List<kotlin.String>
            ) -> kotlin.Unit): ReturnsBuilder {
                this.updateCatImpl = impl
                return this
            }
            
            fun build(): ReturnsImpl = ReturnsImpl(
              fetchCatsImpl = fetchCatsImpl,
              fetchCatByIdImpl = fetchCatByIdImpl,
              catsImpl = catsImpl,
              catByIdImpl = catByIdImpl,
              deleteCatsImpl = deleteCatsImpl,
              updateCatImpl = updateCatImpl
            )
          }
        """.trimIndent()
      )
  }

  @Test
  fun `all methods with parameter should be declarated in ReturnsImpl class`() {
    val generatedFiles = runProcessor("methods.CatRepository", "methods.CatRepositoryFake")

    assertThat(generatedFiles.size).isEqualTo(1)
    val generatedFile = generatedFiles.first()
    val returnsBuilderClass = (generatedFile.members.first() as TypeSpec).typeSpecs.find { it.name == "ReturnsImpl" }
    assertThat(returnsBuilderClass.toString())
      .isEqualToIgnoringWhitespace(
        """
          data class ReturnsImpl(
            val fetchCatsImpl: (() -> kotlin.Unit)?,
            val fetchCatByIdImpl: ((catId: kotlin.String) -> kotlin.Unit)?,
            val catsImpl: (() -> kotlin.collections.List<kotlin.Any>)?,
            val catByIdImpl: ((catId: kotlin.String) -> kotlin.Any)?,
            val deleteCatsImpl: ((catIds: kotlin.collections.Set<kotlin.String>) -> kotlin.Unit)?,
            val updateCatImpl: ((
                catId: kotlin.String,
                newName: kotlin.Any,
                newHomes: kotlin.collections.List<kotlin.String>
            ) -> kotlin.Unit)?
          )
        """.trimIndent()
      )
  }

  @Test
  fun `generated file should contains returns function with ReturnsBuilder init parameter`() {
    val generatedFiles = runProcessor("methods.CatRepository", "methods.CatRepositoryFake")

    assertThat(generatedFiles.size).isEqualTo(1)
    val generatedFile = generatedFiles.first()
    val returnsFun = (generatedFile.members.first() as TypeSpec).funSpecs.find { it.name == "returns" }
    assertThat(returnsFun.toString())
      .isEqualToIgnoringWhitespace(
        """
          fun returns(init: ReturnsBuilder.() -> kotlin.Unit) {
            val builder = ReturnsBuilder()
            builder.init()
            this.returnsImpl = builder.build() 
          }
        """.trimIndent()
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
