package fr.sdis64.dsl

import org.gradle.api.Task
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

@DslMarker
annotation class CtacDsl

@CtacDsl
interface CtacEnvironment {
    val name: Property<String>
    val dockerPrepareTask: Property<Task>
    val dockerSwarmServiceFile: RegularFileProperty
    val useSudoForDeployment: Property<Boolean>
}

@CtacDsl
interface CtacService {
    val name: Property<String>
    fun environments(configure: CtacEnvironments.() -> Unit)
}

@CtacDsl
interface CtacEnvironments {
    fun environment(name: String, configure: CtacEnvironment.() -> Unit)
}

internal abstract class DefaultCtacService @Inject constructor(
    objectFactory: ObjectFactory,
) : CtacService {
    override val name = objectFactory.property<String>()

    internal val environments = DefaultCtacEnvironments(objectFactory)
    override fun environments(configure: CtacEnvironments.() -> Unit) {
        environments.configure()
    }
}

internal class DefaultCtacEnvironments @Inject constructor(
    private val objectFactory: ObjectFactory,
) : CtacEnvironments {
    internal val environmentsByName = objectFactory.domainObjectContainer(CtacEnvironment::class.java)

    override fun environment(name: String, configure: CtacEnvironment.() -> Unit) {
        val env = DefaultCtacEnvironment(objectFactory).also {
            it.name.set(name)
            it.configure()
        }
        environmentsByName.add(env)
    }
}

internal class DefaultCtacEnvironment @Inject constructor(
    objectFactory: ObjectFactory,
) : CtacEnvironment {
    override val name: Property<String> = objectFactory.property<String>()
    override val dockerPrepareTask = objectFactory.property<Task>()
    override val dockerSwarmServiceFile = objectFactory.fileProperty()
    override val useSudoForDeployment = objectFactory.property<Boolean>()
}
