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
