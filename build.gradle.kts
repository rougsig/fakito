import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

var versions: Map<String, String> by extra
var deps: Map<String, String> by extra

versions = mapOf(
  "kotlin" to "1.3.41", // see also plugin block below
  "kotlinpoet" to "1.1.0",
  "kotlinMetadata" to "1.4.0",
  "autoService" to "1.0-rc4",
  "compileTesting" to "0.15",
  "testng" to "6.10",
  "assertjCore" to "3.6.2"
)

deps = mapOf(
  "kotlinStdlib8" to "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${versions["kotlin"]}",
  "kotlinpoet" to "com.squareup:kotlinpoet:${versions["kotlinpoet"]}",
  "kotlinMetadata" to "me.eugeniomarletti.kotlin.metadata:kotlin-metadata:${versions["kotlinMetadata"]}",
  "kotlinReflect" to "org.jetbrains.kotlin:kotlin-reflect:${versions["kotlin"]}",
  "autoService" to "com.google.auto.service:auto-service:${versions["autoService"]}",
  "compileTesting" to "com.google.testing.compile:compile-testing:${versions["compileTesting"]}",
  "testng" to "org.testng:testng:${versions["testng"]}",
  "assertjCore" to "org.assertj:assertj-core:${versions["assertjCore"]}"
)

plugins {
  java
  kotlin("jvm") version "1.3.41"
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
    useJUnitPlatform()
    testLogging {
      exceptionFormat = TestExceptionFormat.FULL
    }
  }

  tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
  }
}
