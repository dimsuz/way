dependencies {
  implementation(kotlin("stdlib-jdk8"))
  implementation(project(":library"))

  testImplementation("io.kotest:kotest-runner-junit5:4.5.0")
  testImplementation("io.kotest:kotest-assertions-core:4.5.0")
  testImplementation("io.kotest:kotest-property:4.5.0")
}

tasks.withType<Test> {
  useJUnitPlatform()
}

tasks {
  compileKotlin {
    kotlinOptions.freeCompilerArgs += listOf("-module-name", "way-sample")
  }
}
