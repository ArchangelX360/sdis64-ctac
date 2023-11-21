pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

includeBuild("gradle-extensions")

include("api")
include("client")
include("backend")
include("gateway")
include("monitoring")
include("resources")
include("ui")

rootProject.name = "ctac"

gradle.beforeProject {
    val localPropertiesFile = rootDir.resolve("local.properties")
    if (!localPropertiesFile.exists()) {
        val template = rootDir.resolve("local.properties.template")
        template.copyTo(localPropertiesFile)
    }
    val localProperties = java.util.Properties()
    localProperties.load(localPropertiesFile.inputStream())
    localProperties.forEach { (k, v) -> if (k is String) project.extra.set(k, v) }
}
