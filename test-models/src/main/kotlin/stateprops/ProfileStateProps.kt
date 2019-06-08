package com.example.mvi.stateprops

import com.example.mvi.entity.CardDetails
import com.example.mvi.entity.LceState
import com.github.rougsig.mvifake.runtime.FakeStateProps
import io.reactivex.Observable

interface ProfileStateProps {
  fun cardDetails(cardId: String): Observable<LceState<CardDetails>>
}

@FakeStateProps(ProfileStateProps::class)
abstract class FakeProfileStateProps
