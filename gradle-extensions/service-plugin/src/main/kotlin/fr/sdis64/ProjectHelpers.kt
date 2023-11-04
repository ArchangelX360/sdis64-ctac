package fr.sdis64

import org.gradle.api.Project
import org.gradle.api.provider.Provider

// FIXME: this should use `ProviderFactory` instead, but because of https://github.com/gradle/gradle/issues/23572
//  we need to find a way to make it work with `local.properties`
private fun Project.getPropOrEnvProvider(propName: String, envVar: String? = null): Provider<Result<String>> {
    val err =
        Result.failure<String>(IllegalStateException("either property '$propName' or environment variable $envVar must be defined"))
    return project.provider { project.findProperty(propName) as String? }.map { s ->
        val p = s.takeIf { it.isNotBlank() } ?: run {
            System.getenv(envVar).takeIf { it.isNotBlank() }
        }
        p?.let { Result.success(it) } ?: err
    }.orElse(err)
}

fun Project.getPropOrEnv(propName: String, envVar: String? = null): String? =
    getPropOrEnvProvider(propName, envVar).get().getOrNull()

fun Project.getMandatoryPropOrEnv(propName: String, envVar: String? = null): String =
    getPropOrEnvProvider(propName, envVar).get().getOrThrow()
