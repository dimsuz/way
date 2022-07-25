plugins {
  id("com.android.application")
  kotlin("android")
  id("com.google.devtools.ksp") version "1.6.10-1.0.4"
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

  applicationVariants.all {
    kotlin {
      sourceSets {
        getByName(name) {
           kotlin.srcDir("build/generated/ksp/$name/kotlin")
        }
      }
    }
  }
}


dependencies {
  implementation("androidx.core:core-ktx:1.8.0")
  implementation("androidx.appcompat:appcompat:1.4.2")
  implementation("androidx.compose.ui:ui:1.1.1")
  implementation("androidx.compose.material:material:1.1.1")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.1")
  implementation("androidx.activity:activity-compose:1.4.0")

  implementation("io.arrow-kt:arrow-optics:1.1.2")
  ksp("io.arrow-kt:arrow-optics-ksp-plugin:1.1.2")

  implementation(project(":way"))
}

tasks.withType<Test> {
  useJUnitPlatform()
}
