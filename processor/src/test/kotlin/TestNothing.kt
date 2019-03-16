package com.github.rougsig.mviautomock.processor

class TestNothing : APTest("com.github.rougsig.mviautomock.processor") {
  fun testNothing() {
    testProcessor(AnnotationProcessor(
      name = "MockProcessor",
      sourceFiles = listOf("Mock.java"),
      destFile = "Mock.kt",
      processor = MockProcessor()
    ))
  }
}
