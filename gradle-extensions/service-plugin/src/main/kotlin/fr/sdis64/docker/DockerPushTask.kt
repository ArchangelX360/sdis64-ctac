package fr.sdis64.docker

import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import org.gradle.process.ExecOperations
import javax.inject.Inject

open class DockerPushTask @Inject constructor(
    private val execOperations: ExecOperations,
    objectFactory: ObjectFactory,
) : DefaultTask() {
    @get:Input
    val tag = objectFactory.property<String>()

    @TaskAction
    fun build() {
        execOperations.exec {
            commandLine("docker", "push", tag.get())
        }
    }
}
