plugins {
  id("com.android.application")
  kotlin("android")
}

android {
  compileSdk = 31

  defaultConfig {
    minSdk = 23
    targetSdk = 30
    versionCode = 1
    versionName = "1.0"
  }

  buildFeatures {
    compose = true
  }

  composeOptions {
    kotlinCompilerExtensionVersion = "1.1.1"
  }
}

dependencies {
  implementation("androidx.core:core-ktx:1.8.0")
  implementation("androidx.appcompat:appcompat:1.4.2")
  implementation("androidx.compose.ui:ui:1.1.1")
  implementation("androidx.compose.material:material:1.1.1")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.1")
  implementation("androidx.activity:activity-compose:1.4.0")

  implementation(project(":way"))
}

tasks.withType<Test> {
  useJUnitPlatform()
}
