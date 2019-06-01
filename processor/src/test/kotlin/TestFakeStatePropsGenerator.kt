package com.github.rougsig.mvifake.processor

class TestFakeStatePropsGenerator : APTest("com.example.mvi.stateprops") {
  fun testLoginStateProps() {
    testProcessor(AnnotationProcessor(
      sourceFiles = listOf("FakeLoginStateProps.java"),
      destinationFile = "FakeLoginStatePropsGenerated.kt.txt",
      processor = MviFakeProcessor()
    ))
  }

  fun testInternalLoginStateProps() {
    testProcessor(AnnotationProcessor(
      sourceFiles = listOf("FakeInternalLoginStateProps.java"),
      destinationFile = "FakeInternalLoginStatePropsGenerated.kt.txt",
      processor = MviFakeProcessor()
    ))
  }

  fun testNestedInternalLoginStateProps() {
    testProcessor(AnnotationProcessor(
      sourceFiles = listOf("FakeNestedInternalLoginStateProps.java"),
      destinationFile = "FakeNestedInternalLoginStatePropsGenerated.kt.txt",
      processor = MviFakeProcessor()
    ))
  }
}
