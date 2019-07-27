# Fakito

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
