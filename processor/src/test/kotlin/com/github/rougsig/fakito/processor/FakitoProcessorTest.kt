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
          |package com.github.rougsig.fakito.processor.testdata.empty
          |
          |abstract class CatRepositoryFakeGenerated : CatRepository
        """.trimMargin()
      )
  }

  @Test
  fun `internal source should generate internal implementation`() {
    val generatedFiles = runProcessor("internal.CatRepository", "internal.CatRepositoryFake")

    assertThat(generatedFiles.size).isEqualTo(1)
    val generatedFile = generatedFiles.first()
    assertThat(generatedFile.toString())
      .isEqualToIgnoringWhitespace(
        """
          |package com.github.rougsig.fakito.processor.testdata.internal
          |
          |internal abstract class CatRepositoryFakeGenerated : CatRepository
        """.trimMargin()
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
          |sealed class Method {
          |  data class CatById(
          |    val catId: kotlin.String
          |  ) : Method()
          |  
          |  object Cats : Method()
          |  
          |  data class DeleteCats(
          |    val catIds: kotlin.collections.Set<kotlin.String>
          |  ) : Method()
          |  
          |  data class FetchCatById(
          |    val catId: kotlin.String
          |  ) : Method()
          |  
          |  object FetchCats : Method()
          |  
          |  data class UpdateCat(
          |      val catId: kotlin.String,
          |      val newName: kotlin.Any?,
          |      val newHomes: kotlin.collections.List<kotlin.String?>
          |  ) : Method()
          |}
        """.trimMargin()
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
          |inner class ReturnsBuilder {
          |  private var catByIdImpl: ((catId: kotlin.String) -> kotlin.Any?)? = null
          |  
          |  private var catsImpl: (() -> kotlin.collections.List<kotlin.Any>)? = null
          |  
          |  private var deleteCatsImpl: ((catIds: kotlin.collections.Set<kotlin.String>) -> kotlin.Unit)? = null
          |
          |  private var fetchCatByIdImpl: ((catId: kotlin.String) -> kotlin.Unit)? = null
          |
          |  private var fetchCatsImpl: (() -> kotlin.Unit)? = null
          |
          |  private var updateCatImpl: ((
          |      catId: kotlin.String,
          |      newName: kotlin.Any?,
          |      newHomes: kotlin.collections.List<kotlin.String?>
          |  ) -> kotlin.Unit)? = null
          |
          |  fun catById(impl: (catId: kotlin.String) -> kotlin.Any?): ReturnsBuilder {
          |      this.catByIdImpl = impl
          |      return this
          |  }
          |
          |  fun cats(impl: () -> kotlin.collections.List<kotlin.Any>): ReturnsBuilder {
          |      this.catsImpl = impl
          |      return this
          |  }
          |  
          |  fun deleteCats(impl: (catIds: kotlin.collections.Set<kotlin.String>) -> kotlin.Unit): ReturnsBuilder {
          |      this.deleteCatsImpl = impl
          |      return this
          |  }
          |  
          |  fun fetchCatById(impl: (catId: kotlin.String) -> kotlin.Unit): ReturnsBuilder {
          |      this.fetchCatByIdImpl = impl
          |      return this
          |  }
          |  
          |  fun fetchCats(impl: () -> kotlin.Unit): ReturnsBuilder {
          |      this.fetchCatsImpl = impl
          |      return this
          |  }
          |
          |  fun updateCat(impl: (
          |      catId: kotlin.String,
          |      newName: kotlin.Any?,
          |      newHomes: kotlin.collections.List<kotlin.String?>
          |  ) -> kotlin.Unit): ReturnsBuilder {
          |      this.updateCatImpl = impl
          |      return this
          |  }
          |  
          |  fun build(): ReturnsImpl = ReturnsImpl(
          |    catByIdImpl,
          |    catsImpl,
          |    deleteCatsImpl,
          |    fetchCatByIdImpl,
          |    fetchCatsImpl,
          |    updateCatImpl
          |  )
          |}
        """.trimMargin()
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
          |data class ReturnsImpl(
          |val catByIdImpl: ((catId: kotlin.String) -> kotlin.Any?)?,
          |val catsImpl: (() -> kotlin.collections.List<kotlin.Any>)?,
          |val deleteCatsImpl: ((catIds: kotlin.collections.Set<kotlin.String>) -> kotlin.Unit)?,
          |val fetchCatByIdImpl: ((catId: kotlin.String) -> kotlin.Unit)?,
          |val fetchCatsImpl: (() -> kotlin.Unit)?,
          |val updateCatImpl: ((
          |  catId: kotlin.String,
          |  newName: kotlin.Any?,
          |  newHomes: kotlin.collections.List<kotlin.String?>
          |) -> kotlin.Unit)?
          |)
        """.trimMargin()
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
          |fun returns(init: ReturnsBuilder.() -> kotlin.Unit) {
          |  val builder = ReturnsBuilder()
          |  builder.init()
          |  this.returnsImpl = builder.build() 
          |}
        """.trimMargin()
      )
  }

  @Test
  fun `generated file should implement target element functions`() {
    val generatedFiles = runProcessor("methods.CatRepository", "methods.CatRepositoryFake")

    assertThat(generatedFiles.size).isEqualTo(1)
    val generatedFile = generatedFiles.first()
    val implFunctions = (generatedFile.members.first() as TypeSpec).funSpecs.filterNot { it.name == "returns" }
    assertThat(implFunctions.joinToString("\n\r"))
      .isEqualToIgnoringWhitespace(
        """
          |override fun catById(catId: kotlin.String): kotlin.Any? {
          |    this.methodCalls.add(Method.CatById(catId))
          |    val classImpl = this.returnsImpl ?: error("returns not found for method catById(catId)")
          |    val methodImpl = classImpl.catByIdImpl ?: error("returns not found for method catById(catId)")
          |    return methodImpl.invoke(catId)
          |}
          |
          |override fun cats(): kotlin.collections.List<kotlin.Any> {
          |    this.methodCalls.add(Method.Cats)
          |    val classImpl = this.returnsImpl ?: error("returns not found for method cats()")
          |    val methodImpl = classImpl.catsImpl ?: error("returns not found for method cats()")
          |    return methodImpl.invoke()
          |}
          |
          |override fun deleteCats(catIds: kotlin.collections.Set<kotlin.String>) {
          |    this.methodCalls.add(Method.DeleteCats(catIds))
          |    returnsImpl?.deleteCatsImpl?.invoke(catIds)
          |}
          |
          |override fun fetchCatById(catId: kotlin.String) {
          |    this.methodCalls.add(Method.FetchCatById(catId))
          |    returnsImpl?.fetchCatByIdImpl?.invoke(catId)
          |}
          |
          |override fun fetchCats() {
          |    this.methodCalls.add(Method.FetchCats)
          |    returnsImpl?.fetchCatsImpl?.invoke()
          |}
          |
          |override fun updateCat(
          |    catId: kotlin.String,
          |    newName: kotlin.Any?,
          |    newHomes: kotlin.collections.List<kotlin.String?>
          |) {
          |    this.methodCalls.add(Method.UpdateCat(catId, newName, newHomes))
          |    returnsImpl?.updateCatImpl?.invoke(catId, newName, newHomes)
          |}
        """.trimMargin()
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
