package fr.sdis64.docker

import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import org.gradle.process.ExecOperations
import javax.inject.Inject

internal abstract class DockerPushTask @Inject constructor(
    private val execOperations: ExecOperations,
    objectFactory: ObjectFactory,
) : DefaultTask() {
    @Input
    val dockerRegistry = objectFactory.property<String>()

    @Input
    val dockerLogin = objectFactory.property<String>()

    @Input
    val dockerPassword = objectFactory.property<String>()

    @Input
    val tag = objectFactory.property<String>()

    @TaskAction
    fun build() {
        dockerPassword.get().byteInputStream().use { passwordStream ->
            execOperations.exec {
                commandLine("docker", "login", dockerRegistry.get(), "--username", dockerLogin.get(), "--password-stdin")
                standardInput = passwordStream
            }
        }
        execOperations.exec {
            commandLine("docker", "push", tag.get())
        }
    }
}
