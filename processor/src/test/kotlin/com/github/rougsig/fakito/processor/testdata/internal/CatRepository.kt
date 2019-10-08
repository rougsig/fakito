package com.github.rougsig.fakito.processor.testdata.internal

import com.github.rougsig.fakito.runtime.Fakito

interface CatRepository

@Fakito(CatRepository::class)
internal interface CatRepositoryFake
