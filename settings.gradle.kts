rootProject.name = "way"
include("way")
include("way-sample")
include("way-sample-android")


pluginManagement {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
  }
}
dependencyResolutionManagement {
  repositories {
    google()
    mavenCentral()
  }
}
