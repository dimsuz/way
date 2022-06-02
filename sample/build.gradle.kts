dependencies {
  implementation(project(":library"))

  testImplementation("io.kotest:kotest-runner-junit5:5.3.0")
  testImplementation("io.kotest:kotest-assertions-core:5.3.0")
  testImplementation("io.kotest:kotest-property:5.3.0")
}

tasks.withType<Test> {
  useJUnitPlatform()
}

tasks {
  compileKotlin {
    kotlinOptions.freeCompilerArgs += listOf("-module-name", "way-sample")
  }
}
