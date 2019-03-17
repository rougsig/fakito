package com.github.rougsig.mvifake.processor

import com.squareup.kotlinpoet.ClassName

val RELAY_CLASS_NAME = ClassName.bestGuess("com.jakewharton.rxrelay2.Relay")
val PUBLISH_RELAY_CLASS_NAME = ClassName.bestGuess("com.jakewharton.rxrelay2.PublishRelay")
val OBSERVABLE_CLASS_NAME = ClassName.bestGuess("io.reactivex.Observable")
