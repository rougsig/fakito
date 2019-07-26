package com.github.rougsig.fakito.processor.core

import com.squareup.kotlinpoet.FileSpec

internal interface Generator<T> {
  fun generateFile(params: T): FileSpec
}
