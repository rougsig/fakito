package com.github.rougsig.mviautomock.processor

class TestMockViewGenerator : APTest("com.example.mvi") {
  fun testNothing() {
    testProcessor(AnnotationProcessor(
      sourceFiles = listOf("LoginView.java"),
      destinationFile = "LoginViewGeneratedMock.kt.txt",
      processor = MviAutoMockProcessor()
    ))
  }
}
