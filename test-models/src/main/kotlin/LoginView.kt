package com.example.mvi

import com.github.rougsig.mviautomock.annotations.MockView

@MockView(consumer = Any::class)
interface LoginView
