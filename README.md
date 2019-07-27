# MVI Fake

# Example
To enable generation, annotate view interface with `@FakeView` annotation:
```kotlin
import io.reactivex.Observable                  // <-- work only with rxjava Observable

interface LoginView {
    fun loginIntent(): Observable<LoginEvent>
    fun dismissValidationErrorIntent(): Observable<Unit>
}

@FakeView(LoginView::class)
class FakeLoginView
```

After doing that you will get an auto-generated `FakeView` abstract class:
```kotlin
import com.jakewharton.rxrelay2.Relay           // <-- require rxrelay2 dependency in your project
import com.jakewharton.rxrelay2.PublishRelay    // <-- require rxrelay2 dependency in your project

abstract class FakeLoginViewGenerated : LoginView {
    protected open fun <T> createDefaultRelay(): Relay<T> {
      return PublishRelay.create<T>()
    }    

    protected open val loginIntent: Relay<LoginEvent> by lazy { createDefaultRelay<LoginEvent>() }

    protected open val dismissValidationErrorIntent: Relay<Unit> by lazy { createDefaultRelay<Unit>() }

    fun sendLoginIntent(value: LoginEvent) {
        loginIntent.accept(value)
    }

    fun sendDismissValidationErrorIntent() {
        dismissValidationErrorIntent.accept(Unit)
    }
    
    override fun loginIntent(): Observable<LoginEvent> = loginIntent
    
    override fun dismissValidationErrorIntent(): Observable<Unit> = dismissValidationErrorIntent
}
```

# Download
Add a Gradle dependency:

```gradle
apply plugin: 'kotlin-kapt'

sourceSets {
    main.java.srcDirs += 'src/main/kotlin'
    main.java.srcDirs += 'build/generated/source/kaptKotlin/main'           // <-- add to your module
    debug.java.srcDirs += 'build/generated/source/kaptKotlin/debug'         // <-- add to your module
    release.java.srcDirs += 'build/generated/source/kaptKotlin/release'     // <-- add to your module
    test.java.srcDirs += 'src/test/kotlin'
}

implementation 'com.github.rougsig:fakito-runtime:2.0.0'
kapt 'com.github.rougsig:fakito-processor:2.0.0'
```
