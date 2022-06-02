plugins {
  kotlin("jvm")
  id("com.android.application") version "7.2.1" apply false
}

dependencies {
  implementation(project(":way"))

  testImplementation("io.kotest:kotest-runner-junit5:5.3.0")
  testImplementation("io.kotest:kotest-assertions-core:5.3.0")
  testImplementation("io.kotest:kotest-property:5.3.0")
}

tasks.withType<Test> {
  useJUnitPlatform()
}
