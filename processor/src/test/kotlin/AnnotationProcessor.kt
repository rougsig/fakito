package com.github.rougsig.mviautomock.processor

import javax.annotation.processing.Processor

data class AnnotationProcessor(
  val name: String,
  val sourceFiles: List<String>,
  val destFile: String? = null,
  val processor: Processor,
  val errorMessage: String? = null
)
