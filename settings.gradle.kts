pluginManagement {
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
        // eIDRomania Android SDK (public read access)
        maven {
            url = uri("https://europe-west1-maven.pkg.dev/eid-romania/eid-romania-sdk")
        }
    }
}

rootProject.name = "eidromania-android-example"
include(":app")
