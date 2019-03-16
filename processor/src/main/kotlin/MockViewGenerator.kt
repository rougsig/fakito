package com.github.rougsig.mviautomock.processor

import com.squareup.kotlinpoet.FileSpec

internal val mockViewGenerator: MockViewGenerator = MockViewGenerator()

internal class MockViewGenerator : Generator<MockViewType> {
  override fun generateFile(type: MockViewType): FileSpec {
    return FileSpec.builder(type.packageName, type.viewName.toString()).build()
  }
}
