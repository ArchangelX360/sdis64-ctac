import fr.sdis64.supportedEnvironments

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
    id("ctac.repositories-conventions")
    id("ctac-service-plugin")
    id("ctac.test-runner-conventions")
}

kotlin {
    js(IR) {
        browser()
        binaries.executable()
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(project(":api"))
                implementation(project(":client"))
                implementation(compose.html.core)
                implementation(compose.html.svg)
                implementation(compose.runtime)
                implementation("app.softwork:routing-compose:0.2.12")
                implementation(libs.kotlinx.datetime)

                compileOnly(npm("html-webpack-plugin", "5.5.0"))
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}

tasks.named("compileKotlinJs") {
    dependsOn(project(":resources").tasks.named("generateUiResourcesComposableSourceFiles"))
}

ctacService {
    name = project.name
    supportedEnvironments.forEach { envName ->
        environments {
            environment(envName) {
                val capitalizedEnvName = envName.replaceFirstChar { it.uppercase() }

                val prepareTask = tasks.register<Sync>("dockerPrepare$capitalizedEnvName") {
                    group = "package"

                    val buildTask = when (envName) {
                        "production" -> tasks.named("jsBrowserDistribution")
                        else -> tasks.named("jsBrowserDevelopmentExecutableDistribution")
                    }
                    dependsOn(buildTask, tasks.named("allTests"))

                    destinationDir = project.buildDir.resolve("docker/${envName}")
                    from(
                        buildTask.map { it.outputs },
                        file("nginx.conf"),
                        file("Dockerfile"),
                    )
                }

                dockerPrepareTask = prepareTask
                dockerSwarmServiceFile = file("${project.name}.$envName.service.yml")
            }
        }
    }
}