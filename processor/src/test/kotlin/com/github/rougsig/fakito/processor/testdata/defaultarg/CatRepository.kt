package com.github.rougsig.fakito.processor.testdata.defaultarg

import com.github.rougsig.fakito.runtime.Fakito

interface CatRepository {
  fun fetchContent(skipCache: Boolean = true)
}

@Fakito(CatRepository::class)
interface CatRepositoryFake
