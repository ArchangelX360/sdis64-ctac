package fr.sdis64.docker

import fr.sdis64.getMandatoryPropOrEnv
import org.gradle.api.Project

internal val Project.dockerRegistry: String
    get() = getMandatoryPropOrEnv("docker.registry", "DOCKER_REGISTRY")

internal val Project.dockerRepository: String
    get() = getMandatoryPropOrEnv("docker.repository", "DOCKER_REPOSITORY")

internal val Project.dockerUser: String
    get() = getMandatoryPropOrEnv("docker.username", "DOCKER_USERNAME")

internal val Project.dockerPassword: String
    get() = getMandatoryPropOrEnv("docker.password", "DOCKER_PASSWORD")

internal fun Project.buildCtacDockerImageTag(serviceName: String, tag: String?): String {
    val image = "$dockerRegistry/$dockerRepository:$serviceName"
    if (tag != null) {
        return "$image-$tag"
    }
    return image
}
