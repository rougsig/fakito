package com.github.rougsig.mviautomock.processor

class TestMockViewGenerator : APTest("com.example.mvi") {
  fun testLoginView() {
    testProcessor(AnnotationProcessor(
      sourceFiles = listOf("MockLoginView.java"),
      destinationFile = "LoginViewGeneratedMock.kt.txt",
      processor = MviAutoMockProcessor()
    ))
  }

  fun testInternalLoginView() {
    testProcessor(AnnotationProcessor(
      sourceFiles = listOf("MockInternalLoginView.java"),
      destinationFile = "InternalLoginViewGeneratedMock.kt.txt",
      processor = MviAutoMockProcessor()
    ))
  }

  fun testNestedInternalLoginView() {
    testProcessor(AnnotationProcessor(
      sourceFiles = listOf("MockNestedInternalLoginView.java"),
      destinationFile = "NestedInternalLoginViewGeneratedMock.kt.txt",
      processor = MviAutoMockProcessor()
    ))
  }
}
