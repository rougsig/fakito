package com.github.rougsig.fakito.processor.testdata.rx

import com.github.rougsig.fakito.runtime.RxFakito
import io.reactivex.Observable

interface CatRepository {
  fun cats(): Observable<List<Any>>
  fun catById(catId: String): Observable<Any>
}

@RxFakito(CatRepository::class)
interface CatRepositoryFake
