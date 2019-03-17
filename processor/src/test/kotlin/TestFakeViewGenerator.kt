package com.github.rougsig.mvifake.processor

class TestFakeViewGenerator : APTest("com.example.mvi") {
  fun testLoginView() {
    testProcessor(AnnotationProcessor(
      sourceFiles = listOf("FakeLoginView.java"),
      destinationFile = "FakeLoginViewGenerated.kt.txt",
      processor = MviFakeProcessor()
    ))
  }

  fun testInternalLoginView() {
    testProcessor(AnnotationProcessor(
      sourceFiles = listOf("FakeInternalLoginView.java"),
      destinationFile = "FakeInternalLoginViewGenerated.kt.txt",
      processor = MviFakeProcessor()
    ))
  }

  fun testNestedInternalLoginView() {
    testProcessor(AnnotationProcessor(
      sourceFiles = listOf("FakeNestedInternalLoginView.java"),
      destinationFile = "FakeNestedInternalLoginViewGenerated.kt.txt",
      processor = MviFakeProcessor()
    ))
  }
}
