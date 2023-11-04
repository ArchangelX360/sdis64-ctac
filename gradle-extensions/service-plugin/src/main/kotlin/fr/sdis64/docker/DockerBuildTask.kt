package fr.sdis64.docker

import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.property
import org.gradle.process.ExecOperations
import javax.inject.Inject

internal abstract class DockerBuildTask @Inject constructor(
    objectFactory: ObjectFactory,
    private val execOps: ExecOperations,
) : DefaultTask() {
    @InputDirectory
    val directory = objectFactory.directoryProperty()

    @Input
    val tag = objectFactory.property<String>()

    @Input
    @Optional
    val platform = objectFactory.property<String>()

    @TaskAction
    fun build() {
        val platform = platform.orNull
        execOps.exec {
            workingDir = directory.get().asFile

            val cmd = listOfNotNull(
                "docker", "buildx", "build",
                if (platform != null) "--platform=$platform" else null,
                "-t", tag.get(),
                ".",
            )
            commandLine(cmd)
        }
    }
}
