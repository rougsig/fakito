package com.github.rougsig.fakito.processor

import org.testng.annotations.Test

internal class FakitoProcessorTest : APTest() {
  @Test
  fun `source without methods should generate implementation without methods`() = testProcessor(
    sourceJava = listOf(
      MemoFile(
        "com.github.rougsig.fakito.test.CatRepository",
        """
          package com.github.rougsig.fakito.test;
  
          public interface CatRepository {
          }
        """
      ),
      MemoFile(
        "com.github.rougsig.fakito.test.CatRepositoryGenerated",
        """
          package com.github.rougsig.fakito.test;
  
          import com.github.rougsig.fakito.runtime.Fakito;
          
          @Fakito(CatRepository.class)
          public class CatRepositoryGenerated {
          }
        """
      )
    ),
    expectedKotlin = listOf(
      MemoFile(
        "com.github.rougsig.fakito.test.CatRepositoryGenerated",
        """
          package com.github.rougsig.fakito.test
          
          open class CatRepositoryGenerated
        """
      )
    )
  )

  private fun testProcessor(
    sourceJava: List<MemoFile>,
    expectedKotlin: List<MemoFile>
  ) {
    testProcessor(
      FakitoProcessor(),
      sourceJava,
      expectedKotlin
    )
  }
}
