package com.github.rougsig.mvifake.processor

class TestFakeDispatchPropsGenerator : APTest("com.example.mvi.dispatchprops") {
  fun testLoginDispatchProps() {
    testProcessor(AnnotationProcessor(
      sourceFiles = listOf("FakeLoginDispatchProps.java"),
      destinationFile = "FakeLoginDispatchPropsGenerated.kt.txt",
      processor = MviFakeProcessor()
    ))
  }

  fun testInternalLoginDispatchProps() {
    testProcessor(AnnotationProcessor(
      sourceFiles = listOf("FakeInternalLoginDispatchProps.java"),
      destinationFile = "FakeInternalLoginDispatchPropsGenerated.kt.txt",
      processor = MviFakeProcessor()
    ))
  }

  fun testNestedInternalLoginDispatchProps() {
    testProcessor(AnnotationProcessor(
      sourceFiles = listOf("FakeNestedInternalLoginDispatchProps.java"),
      destinationFile = "FakeNestedInternalLoginDispatchPropsGenerated.kt.txt",
      processor = MviFakeProcessor()
    ))
  }
}
