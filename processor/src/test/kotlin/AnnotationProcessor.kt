package com.github.rougsig.mviautomock.processor

import javax.annotation.processing.Processor

data class AnnotationProcessor(
  val sourceFiles: List<String>,
  val destinationFile: String? = null,
  val processor: Processor,
  val errorMessage: String? = null
)
