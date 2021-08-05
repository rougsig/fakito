plugins {
  kotlin("kapt")
}

val deps: Map<String, String> by rootProject.extra

dependencies {
  implementation(project(":runtime"))

  implementation(deps.getValue("kotlinpoet")) {
    exclude("org.jetbrains.kotlin:kotlin-reflect")
  }
  implementation(deps.getValue("kotlinpoetMetadata"))
  implementation(deps.getValue("kotlinpoetMetadataSpecs"))
  implementation(deps.getValue("kotlinReflect"))
  implementation(deps.getValue("autoService"))

  kapt(deps.getValue("autoService"))
  kaptTest(deps.getValue("autoService"))

  testImplementation(deps.getValue("rxJava"))
  testImplementation(deps.getValue("rxRelay"))
  testImplementation(deps.getValue("compileTesting")) {
    exclude("junit:junit")
  }
}
