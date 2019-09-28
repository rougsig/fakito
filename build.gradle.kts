import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

var versions: Map<String, String> by extra
var deps: Map<String, String> by extra

versions = mapOf(
  "kotlin" to "1.3.50", // see also plugin block below
  "kotlinpoet" to "1.4.0",
  "autoService" to "1.0-rc4",
  "compileTesting" to "0.15",
  "testng" to "6.10",
  "assertjCore" to "3.6.2",
  "rxJava" to "2.2.12",
  "rxRelay" to "2.1.1"
)

deps = mapOf(
  "kotlinStdlib8" to "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${versions["kotlin"]}",
  "kotlinpoet" to "com.squareup:kotlinpoet:${versions["kotlinpoet"]}",
  "kotlinpoetMetadata" to "com.squareup:kotlinpoet-metadata:${versions["kotlinpoet"]}",
  "kotlinpoetMetadataSpecs" to "com.squareup:kotlinpoet-metadata-specs:${versions["kotlinpoet"]}",
  "kotlinReflect" to "org.jetbrains.kotlin:kotlin-reflect:${versions["kotlin"]}",
  "autoService" to "com.google.auto.service:auto-service:${versions["autoService"]}",
  "compileTesting" to "com.google.testing.compile:compile-testing:${versions["compileTesting"]}",
  "testng" to "org.testng:testng:${versions["testng"]}",
  "assertjCore" to "org.assertj:assertj-core:${versions["assertjCore"]}",
  "rxJava" to "io.reactivex.rxjava2:rxjava:${versions["rxJava"]}",
  "rxRelay" to "com.jakewharton.rxrelay2:rxrelay:${versions["rxRelay"]}"
)

plugins {
  java
  kotlin("jvm") version "1.3.50"
}

allprojects {
  group = property("GROUP")!!
  version = property("VERSION_NAME")!!

  repositories {
    mavenLocal()
    mavenCentral()
  }
}

subprojects {
  apply {
    plugin("kotlin")
  }

  dependencies {
    implementation(deps.getValue("kotlinStdlib8"))

    testImplementation(deps.getValue("testng"))
    testImplementation(deps.getValue("assertjCore"))
  }

  tasks.withType<Test> {
    useTestNG()
    testLogging {
      exceptionFormat = TestExceptionFormat.FULL
    }
  }

  tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
  }
}
