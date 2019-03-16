package com.github.rougsig.mviautomock.processor

import com.squareup.kotlinpoet.ClassName

val relayClassName = ClassName.bestGuess("com.jakewharton.rxrelay2.Relay")
val publishRelayClassName = ClassName.bestGuess("com.jakewharton.rxrelay2.PublishRelay")
val observableClassName = ClassName.bestGuess("io.reactivex.Observable")
