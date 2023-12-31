package fr.sdis64.ssh

import fr.sdis64.getMandatoryPropOrEnv
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import java.nio.file.Path
import java.nio.file.Paths
import javax.inject.Inject
import kotlin.io.path.createDirectories

internal abstract class GenerateConfigTask @Inject constructor(
    objectFactory: ObjectFactory,
    providerFactory: ProviderFactory,
) : DefaultTask() {
    @Input
    val environment = objectFactory.property<String>()

    @OutputDirectory
    val configDirectory = objectFactory.directoryProperty()

    @Internal
    val remoteName: Provider<String> = environment

    @Internal
    val configFilepath: Provider<RegularFile> = configDirectory.file("ssh_config")

    @Internal
    val envSshRemote: Provider<SshRemote> = environment.map {
        project.sshRemote(environment.get())
    }

    @Internal
    val proxySshRemote: Provider<SshRemote> = providerFactory.provider {
        project.sshRemote("proxy")
    }

    @TaskAction
    fun deploy() {
        val sshConfig = SshConfig(
            remoteName = remoteName.get(),
            remote = envSshRemote.get(),
            proxy = proxySshRemote.get(),
        )

        val configFile = configFilepath.get().asFile
        configFile.toPath().parent.createDirectories()
        configFile.writeText(sshConfig.generateConfigFileContent(sshConfig.remote.keyFilepath))
    }
}

private data class SshConfig(
    val remoteName: String,
    val remote: SshRemote,
    val proxy: SshRemote,
) {
    fun generateConfigFileContent(keyFilepath: Path) = """
    StrictHostKeyChecking no
    
    Host sdis_proxy
        Hostname ${proxy.host}
        Port ${proxy.port}
        User ${proxy.username}
        IdentityFile $keyFilepath

    Host $remoteName
        Hostname ${remote.host}
        Port ${remote.port}
        User ${remote.username}
        ProxyJump sdis_proxy
        IdentityFile $keyFilepath
""".trimIndent()
}

internal data class SshRemote(
    val host: String,
    val port: Int,
    val username: String,
    val keyFilepath: Path,
)

private fun Project.sshRemote(name: String) = SshRemote(
    host = getMandatorySshProp(name, "host"),
    port = getMandatorySshProp(name, "port").toInt(),
    username = getMandatorySshProp(name, "username"),
    keyFilepath = Paths.get(getMandatorySshProp(name, "keyFilepath")),
)

private fun Project.getMandatorySshProp(remote: String, prop: String) = getMandatoryPropOrEnv(
    "ssh.remotes.${remote.lowercase()}.${prop}", "SSH_${remote.uppercase()}_${prop.uppercase()}"
)
