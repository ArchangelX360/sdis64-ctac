package fr.sdis64.docker

import fr.sdis64.getMandatoryPropOrEnv
import org.gradle.api.Project

val Project.dockerRegistry: String
    get() = getMandatoryPropOrEnv("docker.registry", "DOCKER_REGISTRY")

val Project.dockerRepository: String
    get() = getMandatoryPropOrEnv("docker.repository", "DOCKER_REPOSITORY")

val Project.dockerUser: String
    get() = getMandatoryPropOrEnv("docker.username", "DOCKER_USERNAME")

val Project.dockerPassword: String
    get() = getMandatoryPropOrEnv("docker.password", "DOCKER_PASSWORD")

fun Project.buildCtacDockerImageTag(serviceName: String, tag: String?): String {
    val image = "$dockerRegistry/$dockerRepository:$serviceName"
    if (tag != null) {
        return "$image-$tag"
    }
    return image
}
