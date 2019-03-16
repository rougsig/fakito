package com.github.rougsig.mviautomock.processor

class TestNothing : APTest("com.example.mvi") {
  fun testNothing() {
    testProcessor(AnnotationProcessor(
      sourceFiles = listOf("LoginView.java"),
      destinationFile = "LoginViewGeneratedMock.kt",
      processor = MockProcessor()
    ))
  }
}
