package com.github.rougsig.mvifake.processor

import com.squareup.kotlinpoet.FileSpec

internal interface Generator<T> {
  fun generateFile(type: T): FileSpec
}
