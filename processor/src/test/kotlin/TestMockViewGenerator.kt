package com.github.rougsig.mviautomock.processor

class TestMockViewGenerator : APTest("com.example.mvi") {
  fun testLoginView() {
    testProcessor(AnnotationProcessor(
      sourceFiles = listOf("LoginView.java"),
      destinationFile = "LoginViewGeneratedMock.kt.txt",
      processor = MviAutoMockProcessor()
    ))
  }

  fun testInternalLoginView() {
    testProcessor(AnnotationProcessor(
      sourceFiles = listOf("InternalLoginView.java"),
      destinationFile = "InternalLoginViewGeneratedMock.kt.txt",
      processor = MviAutoMockProcessor()
    ))
  }
}
