package fr.sdis64

import com.ulisesbocchio.jasyptspringboot.encryptor.SimpleAsymmetricConfig
import com.ulisesbocchio.jasyptspringboot.encryptor.SimpleAsymmetricStringEncryptor
import com.ulisesbocchio.jasyptspringboot.util.AsymmetricCryptography
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.register
import java.security.KeyPairGenerator
import java.util.*
import javax.inject.Inject
import kotlin.io.path.absolutePathString

const val GROUP = "configuration-encryption"

interface CtacConfigurationEncryptionExtension {
    val publicKeyFile: RegularFileProperty
    val privateKeyFile: RegularFileProperty
}

class CtacConfigurationEncryptionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create<CtacConfigurationEncryptionExtension>("configurationEncryption")

        project.tasks.register<GenerateKeysTask>("generateKeys") {
            group = GROUP

            publicKeyFile.set(extension.publicKeyFile)
            privateKeyFile.set(extension.privateKeyFile)
        }

        project.tasks.register<EncryptConfigurationTask>("encrypt") {
            group = GROUP

            publicKeyFile.set(extension.publicKeyFile)
        }

        project.tasks.register<DecryptConfigurationTask>("decrypt") {
            group = GROUP

            privateKeyFile.set(extension.privateKeyFile)
        }
    }
}

open class GenerateKeysTask @Inject constructor(
    objectFactory: ObjectFactory,
) : DefaultTask() {
    @get:OutputFile
    val publicKeyFile = objectFactory.fileProperty()

    @get:OutputFile
    val privateKeyFile = objectFactory.fileProperty()

    @TaskAction
    fun execute() {
        val kpg = KeyPairGenerator.getInstance("RSA")
        kpg.initialize(2048)
        val kp = kpg.generateKeyPair()

        val privateKeyFile = privateKeyFile.get().asFile
        privateKeyFile.writeText(
            """-----BEGIN PRIVATE KEY-----
            |${Base64.getMimeEncoder().encodeToString(kp.private.encoded)}
            |-----END PRIVATE KEY-----""".trimMargin()
        )
        logger.lifecycle("Generate private key at location: $privateKeyFile")

        val publicKeyFile = publicKeyFile.get().asFile
        publicKeyFile.writeText(
            """-----BEGIN PUBLIC KEY-----
            |${Base64.getMimeEncoder().encodeToString(kp.public.encoded)}
            |-----END PUBLIC KEY-----""".trimMargin()
        )
        logger.lifecycle("Generate public key at location: $publicKeyFile")
    }
}

open class EncryptConfigurationTask @Inject constructor(
    objectFactory: ObjectFactory,
) : DefaultTask() {
    @get:Input
    @get:Option(option = "value", description = "configuration value to encrypt")
    val valueToEncrypt = objectFactory.property<String>()

    @get:InputFile
    val publicKeyFile = objectFactory.fileProperty()

    @TaskAction
    fun execute() {
        val encryptor = SimpleAsymmetricStringEncryptor(SimpleAsymmetricConfig().apply {
            publicKeyLocation = "file:${publicKeyFile.get().asFile.toPath().absolutePathString()}"
            publicKeyFormat = AsymmetricCryptography.KeyFormat.PEM
        })
        val encrypted = encryptor.encrypt(valueToEncrypt.get())
        println(encrypted)
    }
}

open class DecryptConfigurationTask @Inject constructor(
    objectFactory: ObjectFactory,
) : DefaultTask() {
    @get:Input
    @get:Option(option = "value", description = "configuration value to decrypt")
    val encryptedValue = objectFactory.property<String>()

    @get:InputFile
    val privateKeyFile = objectFactory.fileProperty()

    @TaskAction
    fun execute() {
        val encryptor = SimpleAsymmetricStringEncryptor(SimpleAsymmetricConfig().apply {
            privateKeyLocation = "file:${privateKeyFile.get().asFile.toPath().absolutePathString()}"
            privateKeyFormat = AsymmetricCryptography.KeyFormat.PEM
        })
        val encrypted = encryptor.decrypt(encryptedValue.get())
        println(encrypted)
    }
}
