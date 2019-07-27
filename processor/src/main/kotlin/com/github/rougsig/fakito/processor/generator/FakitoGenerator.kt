package com.github.rougsig.fakito.processor.generator

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec

object FakitoGenerator : Generator<FakitoGenerator.Params> {
  data class Params(
    val packageName: String,
    val fileName: String
  )

  override fun generateFile(params: Params): FileSpec {
    return FileSpec.get(params.packageName,
      TypeSpec
        .classBuilder(params.fileName)
        .addModifiers(KModifier.OPEN)
        .build()
    )
  }
}
