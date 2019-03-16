package com.github.rougsig.mviautomock.processor

class TestMockViewGenerator : APTest("com.example.mvi") {
  fun testLoginView() {
    testProcessor(AnnotationProcessor(
      sourceFiles = listOf("MockLoginView.java"),
      destinationFile = "MockLoginViewGenerated.kt.txt",
      processor = MviAutoMockProcessor()
    ))
  }

  fun testInternalLoginView() {
    testProcessor(AnnotationProcessor(
      sourceFiles = listOf("MockInternalLoginView.java"),
      destinationFile = "MockInternalLoginViewGenerated.kt.txt",
      processor = MviAutoMockProcessor()
    ))
  }

  fun testNestedInternalLoginView() {
    testProcessor(AnnotationProcessor(
      sourceFiles = listOf("MockNestedInternalLoginView.java"),
      destinationFile = "MockNestedInternalLoginViewGenerated.kt.txt",
      processor = MviAutoMockProcessor()
    ))
  }
}
