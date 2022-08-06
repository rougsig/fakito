import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

var versions: Map<String, String> by extra
var deps: Map<String, String> by extra

versions = mapOf(
  "kotlin" to "1.7.10", // see also plugin block below
  "kotlinpoet" to "1.12.0",
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
  kotlin("jvm") version "1.7.10"
  `maven-publish`
  signing
  id("org.jetbrains.dokka") version "1.4.0-rc"
}

allprojects {
  repositories {
    mavenCentral()
    jcenter()
  }
}

subprojects {
  apply {
    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "signing")
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

  tasks {
    compileKotlin {
      kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
      kotlinOptions.jvmTarget = "1.8"
    }
  }

  val dokkaJar by tasks.creating(org.gradle.jvm.tasks.Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    archiveClassifier.set("javadoc")
    from(tasks.dokkaHtml)
  }

  val sourcesJar by tasks.creating(org.gradle.jvm.tasks.Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
  }

  val pomArtifactId: String? by project
  if (pomArtifactId != null) {
    publishing {
      publications {
        create<MavenPublication>("maven") {
          val versionName: String by project
          val pomGroupId: String by project
          groupId = pomGroupId
          artifactId = pomArtifactId
          version = versionName
          from(components["java"])

          artifact(dokkaJar)
          artifact(sourcesJar)

          pom {
            val pomDescription: String by project
            val pomUrl: String by project
            val pomName: String by project
            description.set(pomDescription)
            url.set(pomUrl)
            name.set(pomName)
            scm {
              val pomScmUrl: String by project
              val pomScmConnection: String by project
              val pomScmDevConnection: String by project
              url.set(pomScmUrl)
              connection.set(pomScmConnection)
              developerConnection.set(pomScmDevConnection)
            }
            licenses {
              license {
                val pomLicenseName: String by project
                val pomLicenseUrl: String by project
                val pomLicenseDist: String by project
                name.set(pomLicenseName)
                url.set(pomLicenseUrl)
                distribution.set(pomLicenseDist)
              }
            }
            developers {
              developer {
                val pomDeveloperId: String by project
                val pomDeveloperName: String by project
                id.set(pomDeveloperId)
                name.set(pomDeveloperName)
              }
            }
          }
        }
      }
      signing {
        sign(publishing.publications["maven"])
      }
      repositories {
        maven {
          val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
          val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
          val versionName: String by project
          url = if (versionName.endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
          credentials {
            username = project.property("NEXUS_USERNAME")?.toString()
            password = project.property("NEXUS_PASSWORD")?.toString()
          }
        }
      }
    }
  }
}
