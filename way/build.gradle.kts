plugins {
  kotlin("jvm")
  `maven-publish`
  signing
  id("org.jetbrains.dokka") version "1.5.31"
}

dependencies {
  api("com.michael-bull.kotlin-result:kotlin-result:1.1.16")

  testImplementation("io.kotest:kotest-runner-junit5:5.3.0")
  testImplementation("io.kotest:kotest-assertions-core:5.3.0")
  testImplementation("io.kotest:kotest-property:5.3.0")
  testImplementation("com.jakewharton.picnic:picnic:0.6.0")
}

tasks.withType<Test> {
  useJUnitPlatform()
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
          username = project.findProperty("NEXUS_USERNAME")?.toString()
          password = project.findProperty("NEXUS_PASSWORD")?.toString()
        }
      }
    }
  }
}
