package com.github.rougsig.fakito.processor

import org.testng.annotations.Test

internal class FakitoProcessorTest : APTest() {
  @Test
  fun nothing() = testProcessor(
    source = MemoFile(
      name = "Main"
    ),
    expected = null
  )

  private fun testProcessor(
    source: MemoFile,
    expected: MemoFile?
  ) {
    testProcessor(
      FakitoProcessor(),
      source,
      expected
    )
  }
}
