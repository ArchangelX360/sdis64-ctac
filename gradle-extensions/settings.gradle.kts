dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

include("configuration-encryption-plugin")
include("ctac-conventions")
include("service-plugin")
