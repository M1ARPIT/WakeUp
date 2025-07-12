pluginManagement {
    plugins {
        id("org.jetbrains.kotlin.plugin.compose") version "1.5.3" // ✅ Already there
        id("com.google.gms.google-services") version "4.4.1"       // ✅ ADD THIS LINE
    }
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "WakeUp"
include(":app")
