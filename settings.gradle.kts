pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://asia-northeast3-maven.pkg.dev/moment-prod-16047/moment-x")
            // TODO: Replace MAVEN_SECRET.
            credentials {
                this.username = "_json_key_base64"
                this.password = "MAVEN_SECRET"
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}

include("app")
