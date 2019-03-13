package com.github.rougsig.mviautomock.processor

import junit.framework.TestCase
import kompile.testing.kotlinc

class TestNothing : TestCase() {
  fun testNothing() {
    kotlinc()
      .withProcessors(MockProcessor())
      .addKotlin("input.kt", """
        import kompile.testing.TestAnnotation

        @TestAnnotation
        class TestClass
        """.trimIndent())
      .compile()
  }
}
