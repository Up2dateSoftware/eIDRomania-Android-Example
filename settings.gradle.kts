pluginManagement {
    plugins {
        id("com.google.cloud.artifactregistry.gradle-plugin") version "2.2.1"
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
        maven {
            url = uri("https://europe-west1-maven.pkg.dev/eid-romania/eid-romania-sdk")
            credentials {
                username = "_json_key_base64"
                password = "YOUR_SDK_KEY_HERE" // provided by Up2Date Software SRL with your license
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}

rootProject.name = "eidromania-android-example"
include(":app")
