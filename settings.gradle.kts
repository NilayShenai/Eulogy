pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Guardian Project Maven - primary source
        maven {
            url = uri("https://raw.githubusercontent.com/guardianproject/gpmaven/master")
            content {
                includeGroup("info.guardianproject")
            }
            isAllowInsecureProtocol = false
        }
        // Maven Central as fallback
        mavenCentral()
        // JitPack as additional fallback
        maven {
            url = uri("https://jitpack.io")
            content {
                includeGroup("info.guardianproject")
            }
        }
    }
}

rootProject.name = "bitchat-android"
include(":app")
// Using published Arti AAR; local module not included
