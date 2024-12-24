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
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "FlySightCompanion"
include(":app")
include(":framework")
include(":BluetoothModule")
include(":FSDeviceModule")
include(":model")
include(":ConfigFilesModule")
include(":ComposableCommons")
include(":Middleware:UserPreferencesModule")
include(":DesignSystem")
include(":Middleware:DialogModule")
