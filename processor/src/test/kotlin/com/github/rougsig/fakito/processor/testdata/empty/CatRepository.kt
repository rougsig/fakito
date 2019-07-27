package com.github.rougsig.fakito.processor.testdata.empty

import com.github.rougsig.fakito.runtime.Fakito

interface CatRepository

@Fakito(CatRepository::class)
interface CatRepositoryFake
