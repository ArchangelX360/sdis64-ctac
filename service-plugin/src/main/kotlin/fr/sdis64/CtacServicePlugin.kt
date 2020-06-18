package fr.sdis64

import fr.sdis64.docker.*
import fr.sdis64.dsl.CtacEnvironment
import fr.sdis64.dsl.DefaultCtacService
import fr.sdis64.ssh.ExecSshTask
import fr.sdis64.ssh.GenerateConfigTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.closureOf
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

private const val stackName = "sdis64"
val supportedEnvironments = listOf("staging", "production")

class CtacServicePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val service = project.extensions.create<DefaultCtacService>("ctacService")

        service.environments.environmentsByName.whenObjectAdded(closureOf<CtacEnvironment> {
            val env = this
            val envName = env.name.get()
            val capitalizedEnvName = envName.replaceFirstChar { it.uppercase() }

            val serviceName = service.name

            val dockerTag = service.name.map { service -> project.buildCtacDockerImageTag(service, envName) }

            val dockerPrepareTaskOutputDirectory =
                env.dockerPrepareTask.map { task -> task.outputs.files.singleFile }.let { project.layout.dir(it) }

            project.tasks.register<DockerBuildTask>("dockerBuild${capitalizedEnvName}Local") {
                dependsOn(env.dockerPrepareTask)
                group = "package"

                description = "Build $envName docker image of service for local platform"

                tag.set(dockerTag.map { "$it-local" })
                directory.set(dockerPrepareTaskOutputDirectory)
            }

            val dockerBuildTask = project.tasks.register<DockerBuildTask>("dockerBuild$capitalizedEnvName") {
                dependsOn(env.dockerPrepareTask)
                group = "package"

                description = "Build $envName docker image of service for target platform (Linux x86_64)"

                tag.set(dockerTag)
                directory.set(dockerPrepareTaskOutputDirectory)
                platform.set("linux/amd64")
            }

            val dockerPushTask = project.tasks.register<DockerPushTask>("dockerPush$capitalizedEnvName") {
                dependsOn(dockerBuildTask)
                group = "deploy"
                tag.set(dockerBuildTask.flatMap { it.tag })
            }

            val generateSshConfigTask =
                project.tasks.register<GenerateConfigTask>("generateSshConfig$capitalizedEnvName") {
                    group = "deploy"

                    environment.set(envName)
                    configDirectory.set(project.layout.buildDirectory.dir("ssh/$envName"))
                }

            project.tasks.register<ExecSshTask>("deploy$capitalizedEnvName") {
                dependsOn(generateSshConfigTask, dockerPushTask)
                group = "deploy"

                val commandsProvider = serviceName.map { service ->
                    val tag = dockerTag.get()
                    val sudo = if (env.useSudoForDeployment.getOrElse(false)) "sudo " else ""
                    val updateImageOnHostCommands = listOf(
                        "${sudo}docker login -u ${project.dockerUser} -p ${project.dockerPassword} ${project.dockerRegistry}",
                        "${sudo}docker pull $tag",
                    )
                    val dockerSwarmServiceFileContent = env.dockerSwarmServiceFile.map { it.asFile.readText() }.get()
                    val deployCommand = if (project.getPropOrEnv("deployStack").toBoolean()) {
                        "echo \"${dockerSwarmServiceFileContent}\" | ${sudo}docker stack deploy --with-registry-auth --compose-file=- $stackName"
                    } else {
                        "${sudo}docker service update --with-registry-auth --image $tag ${stackName}_$service"
                    }
                    updateImageOnHostCommands + deployCommand
                }

                configFilepath.set(generateSshConfigTask.flatMap { it.configFilepath })
                remoteName.set(generateSshConfigTask.flatMap { it.remoteName })
                commands.set(commandsProvider)
            }
        })
    }
}
