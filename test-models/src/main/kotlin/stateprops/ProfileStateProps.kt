package com.example.mvi.stateprops

import com.example.mvi.entities.CardDetails
import com.example.mvi.entities.LceState
import com.github.rougsig.mvifake.runtime.FakeStateProps
import io.reactivex.Observable

interface ProfileStateProps {
  fun cardDetails(cardId: String): Observable<LceState<CardDetails>>
}

@FakeStateProps(ProfileStateProps::class)
abstract class FakeProfileStateProps
