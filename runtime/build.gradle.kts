val deps: Map<String, String> by rootProject.extra

dependencies {
}

apply("../gradle/gradle-mvn-push.gradle")
