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
include(":Tooling:framework")
include(":Middleware:BluetoothModule")
include(":Feature:FSDeviceModule")
include(":model")
include(":Feature:ConfigFilesModule")
include(":Tooling:ComposableCommons")
include(":Feature:UserPreferencesModule")
include(":Tooling:DesignSystem")
include(":Tooling:DialogModule")
include(":Feature:RecordsModule")
include(":Tooling:NetworkModule")
include(":Feature:FirmwareModule")
