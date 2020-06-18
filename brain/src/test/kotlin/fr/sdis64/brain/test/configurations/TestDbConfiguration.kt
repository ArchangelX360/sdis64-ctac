package fr.sdis64.brain.test.configurations

import fr.sdis64.brain.BrainApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

@Configuration
@EnableAutoConfiguration
@EntityScan(basePackageClasses = [BrainApplication::class])
@EnableJpaRepositories(basePackageClasses = [BrainApplication::class])
class TestDbConfiguration {

    companion object {
        val postGreSQLContainer = GenericContainer<Nothing>(DockerImageName.parse("postgres:12.4")).apply {
            withEnv("POSTGRES_DB", TEST_DB_NAME)
            withEnv("POSTGRES_USER", TEST_DB_USERNAME)
            withEnv("POSTGRES_PASSWORD", TEST_DB_PASSWORD)
            withExposedPorts(TEST_DB_PORT)
        }
    }

    internal class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
            postGreSQLContainer.start()

            TestPropertyValues.of(
                    "spring.datasource.url=jdbc:postgresql://${postGreSQLContainer.host}:${postGreSQLContainer.firstMappedPort}/${TEST_DB_NAME}",
                    "spring.datasource.username=$TEST_DB_USERNAME",
                    "spring.datasource.password=$TEST_DB_PASSWORD",
                    "spring.datasource.driver-class-name=org.postgresql.Driver",
                    "spring.jpa.hibernate.ddl-auto=create",
            ).applyTo(configurableApplicationContext.environment)
        }
    }
}

private const val TEST_DB_NAME = "ctac"
private const val TEST_DB_PORT = 5432
private const val TEST_DB_USERNAME = "ctac"
private const val TEST_DB_PASSWORD = "testtest"
