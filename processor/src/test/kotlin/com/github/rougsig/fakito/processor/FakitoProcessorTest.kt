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
          |public abstract class CatRepositoryFakeGenerated : CatRepository
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
          |package com.github.rougsig.fakito.processor.testdata.`internal`
          |
          |internal abstract class CatRepositoryFakeGenerated : CatRepository
        """.trimMargin()
      )
  }

  @Test
  fun `methods with default args should generate successfully`() {
    val generatedFiles = runProcessor("defaultarg.CatRepository", "defaultarg.CatRepositoryFake")

    assertThat(generatedFiles.size).isEqualTo(1)
    val generatedFile = generatedFiles.first()
    val implFunctions = (generatedFile.members.first() as TypeSpec).funSpecs.filterNot { it.name == "returns" }
    assertThat(implFunctions.joinToString("\n\r"))
      .isEqualToIgnoringWhitespace(
        """
        |public override fun fetchContent(skipCache: kotlin.Boolean): kotlin.Unit {
        |  this.methodCalls.add(Method.FetchContent(skipCache))
        |  returnsImpl?.fetchContentImpl?.invoke(skipCache)
        |}
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
          |public sealed class Method {
          |  public data class CatById(
          |    public val catId: kotlin.String,
          |  ) : Method()
          |  
          |  public object Cats : Method()
          |  
          |  public data class DeleteCats(
          |    public val catIds: kotlin.collections.Set<kotlin.String>,
          |  ) : Method()
          |  
          |  public data class FetchCatById(
          |    public val catId: kotlin.String,
          |  ) : Method()
          |  
          |  public object FetchCats : Method()
          |  
          |  public data class UpdateCat(
          |      public val catId: kotlin.String,
          |      public val newName: kotlin.Any?,
          |      public val newHomes: kotlin.collections.List<kotlin.String?>,
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
          |public inner class ReturnsBuilder {
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
          |    catId: kotlin.String,
          |    newName: kotlin.Any?,
          |    newHomes: kotlin.collections.List<kotlin.String?>,
          |  ) -> kotlin.Unit)? = null
          |
          |  public fun catById(`impl`: (catId: kotlin.String) -> kotlin.Any?): ReturnsBuilder {
          |    this.catByIdImpl = impl
          |    return this
          |  }
          |
          |  public fun cats(`impl`: () -> kotlin.collections.List<kotlin.Any>): ReturnsBuilder {
          |    this.catsImpl = impl
          |    return this
          |  }
          |
          |  public fun deleteCats(`impl`: (catIds: kotlin.collections.Set<kotlin.String>) -> kotlin.Unit): ReturnsBuilder {
          |    this.deleteCatsImpl = impl
          |    return this
          |  }
          |
          |  public fun fetchCatById(`impl`: (catId: kotlin.String) -> kotlin.Unit): ReturnsBuilder {
          |    this.fetchCatByIdImpl = impl
          |    return this
          |  }
          |
          |  public fun fetchCats(`impl`: () -> kotlin.Unit): ReturnsBuilder {
          |    this.fetchCatsImpl = impl
          |    return this
          |  }
          |
          |  public fun updateCat(`impl`: (
          |    catId: kotlin.String,
          |    newName: kotlin.Any?,
          |    newHomes: kotlin.collections.List<kotlin.String?>,
          |  ) -> kotlin.Unit): ReturnsBuilder {
          |    this.updateCatImpl = impl
          |    return this
          |  }
          |
          |  public fun build(): ReturnsImpl = ReturnsImpl(
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
          |public data class ReturnsImpl(
          |  public val catByIdImpl: ((catId: kotlin.String) -> kotlin.Any?)?,
          |  public val catsImpl: (() -> kotlin.collections.List<kotlin.Any>)?,
          |  public val deleteCatsImpl: ((catIds: kotlin.collections.Set<kotlin.String>) -> kotlin.Unit)?,
          |  public val fetchCatByIdImpl: ((catId: kotlin.String) -> kotlin.Unit)?,
          |  public val fetchCatsImpl: (() -> kotlin.Unit)?,
          |  public val updateCatImpl: ((
          |    catId: kotlin.String,
          |    newName: kotlin.Any?,
          |    newHomes: kotlin.collections.List<kotlin.String?>,
          |  ) -> kotlin.Unit)?,
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
          |public fun returns(`init`: ReturnsBuilder.() -> kotlin.Unit): kotlin.Unit {
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
          |public override fun catById(catId: kotlin.String): kotlin.Any? {
          |    this.methodCalls.add(Method.CatById(catId))
          |    val classImpl = this.returnsImpl ?: error("returns not found for method catById(catId)")
          |    val methodImpl = classImpl.catByIdImpl ?: error("returns not found for method catById(catId)")
          |    return methodImpl.invoke(catId)
          |}
          |
          |public override fun cats(): kotlin.collections.List<kotlin.Any> {
          |    this.methodCalls.add(Method.Cats)
          |    val classImpl = this.returnsImpl ?: error("returns not found for method cats()")
          |    val methodImpl = classImpl.catsImpl ?: error("returns not found for method cats()")
          |    return methodImpl.invoke()
          |}
          |
          |public override fun deleteCats(catIds: kotlin.collections.Set<kotlin.String>): kotlin.Unit {
          |    this.methodCalls.add(Method.DeleteCats(catIds))
          |    returnsImpl?.deleteCatsImpl?.invoke(catIds)
          |}
          |
          |public override fun fetchCatById(catId: kotlin.String): kotlin.Unit {
          |    this.methodCalls.add(Method.FetchCatById(catId))
          |    returnsImpl?.fetchCatByIdImpl?.invoke(catId)
          |}
          |
          |public override fun fetchCats(): kotlin.Unit {
          |    this.methodCalls.add(Method.FetchCats)
          |    returnsImpl?.fetchCatsImpl?.invoke()
          |}
          |
          |public override fun updateCat(
          |    catId: kotlin.String,
          |    newName: kotlin.Any?,
          |    newHomes: kotlin.collections.List<kotlin.String?>,
          |): kotlin.Unit {
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
