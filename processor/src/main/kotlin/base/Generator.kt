package com.github.rougsig.mvifake.processor.base

import com.squareup.kotlinpoet.FileSpec

internal interface Generator<T> {
  fun generateFile(type: T): FileSpec
}
