dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

includeBuild("ctac-conventions")
include("configuration-encryption-plugin")
include("service-plugin")
