plugins {
  kotlin("kapt")
}

val deps: Map<String, String> by rootProject.extra

dependencies {
  implementation(project(":runtime"))

  implementation(deps.getValue("kotlinMetadata"))
  implementation(deps.getValue("kotlinpoet")) {
    exclude("org.jetbrains.kotlin:kotlin-reflect")
  }
  implementation(deps.getValue("kotlinReflect"))
  implementation(deps.getValue("autoService"))

  kapt(deps.getValue("autoService"))

  testImplementation(deps.getValue("compileTesting")) {
    exclude("junit:junit")
  }
}

apply("../gradle/gradle-mvn-push.gradle")
