package fr.sdis64.ssh

import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import org.gradle.process.ExecOperations
import javax.inject.Inject

open class ExecSshTask @Inject constructor(
    private val execOperations: ExecOperations,
    objectFactory: ObjectFactory,
) : DefaultTask() {
    @get:Input
    val remoteName = objectFactory.property<String>()

    @get:InputFile
    val configFilepath = objectFactory.fileProperty()

    @get:Input
    val commands = objectFactory.listProperty<String>()

    @TaskAction
    fun deploy() {
        execOperations.exec {
            val commands = commands.get().joinToString(" && ")
            commandLine("ssh", "-F", configFilepath.get(), remoteName.get(), commands)
        }
    }
}
