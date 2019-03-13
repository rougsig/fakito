package com.github.rougsig.mviautomock.processor

import com.squareup.kotlinpoet.FileSpec

interface Generator {
  fun generateFile(): FileSpec
}
