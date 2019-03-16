package com.github.rougsig.mviautomock.processor

class TestNothing : APTest("com.example.mvi") {
  fun testNothing() {
    testProcessor(AnnotationProcessor(
      name = "MockProcessor",
      sourceFiles = listOf("LoginView.java"),
      destFile = "LoginView.kt",
      processor = MockProcessor()
    ))
  }
}
