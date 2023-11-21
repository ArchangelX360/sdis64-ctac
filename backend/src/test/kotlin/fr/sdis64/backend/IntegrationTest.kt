package fr.sdis64.backend

import fr.sdis64.api.indicators.ManualIndicatorType
import fr.sdis64.backend.auth.LdapConfiguration
import fr.sdis64.backend.auth.SecurityController
import fr.sdis64.backend.auth.WebSecurityConfigurer
import fr.sdis64.backend.indicators.ManualIndicatorCategoryController
import fr.sdis64.backend.indicators.ManualIndicatorCategoryRepository
import fr.sdis64.backend.indicators.entities.ManualIndicatorCategory
import fr.sdis64.backend.test.configurations.TestDbConfiguration
import fr.sdis64.backend.test.configurations.TestLdapConfigurer
import fr.sdis64.client.CtacClient
import io.ktor.client.plugins.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ConfigurationPropertiesScan(basePackageClasses = [LdapConfiguration::class])
@ContextConfiguration(
        classes = [
            TestDbConfiguration::class,
            TestLdapConfigurer::class,
            WebSecurityConfigurer::class,
            SecurityController::class,
        ],
        initializers = [TestDbConfiguration.Initializer::class],
)
abstract class IntegrationTest {
    @LocalServerPort
    private val randomServerPort: Int = 0

    protected suspend fun newClient() = CtacClient("http://localhost:$randomServerPort")

    @OptIn(ExperimentalCoroutinesApi::class)
    protected fun runTestWithClient(block: suspend CoroutineScope.(CtacClient) -> Unit) = runTest {
        block(newClient())
    }
}

class LoginTests : IntegrationTest() {
    @Test
    fun `should login`() = runTestWithClient { client ->
        client.login("rick", "rickspassword")
        assertEquals("rick", client.currentSession().value?.username)
    }

    @Test
    fun `should login admin`() = runTestWithClient { client ->
        client.login("admin-user", "admin-password")
        assertEquals("admin-user", client.currentSession().value?.username)
    }
}

@ContextConfiguration(classes = [ManualIndicatorCategoryController::class])
class MutationEndpointTests : IntegrationTest() {
    @Autowired
    private lateinit var manualIndicatorCategoryRepository: ManualIndicatorCategoryRepository

    @Test
    fun `should forbid normal user to mutate something`() = runTestWithClient { client ->
        val id = saveMockIndicatorCategory()

        client.login("rick", "rickspassword")

        val exception = assertThrows<ClientRequestException> {
            val category = client.findManualIndicatorCategory(id)
            client.saveManualIndicatorCategory(category.copy(name = "New name"))
        }

        assertEquals(HttpStatusCode.Forbidden, exception.response.status)
    }

    @Test
    fun `should allow admin user to mutate something`() = runTestWithClient { client ->
        val id = saveMockIndicatorCategory()

        client.login("admin-user", "admin-password")

        val category = client.findManualIndicatorCategory(id)
        val saved = client.saveManualIndicatorCategory(category.copy(name = "New name"))

        assertNotEquals(saved.name, category.name)
    }

    private fun saveMockIndicatorCategory(): Long {
        val indicator = ManualIndicatorCategory(
                name = "Test Category",
                type = ManualIndicatorType.ORDRE_PUBLIC,
        ).apply {
            id = 1L
        }
        val saved = manualIndicatorCategoryRepository.save(indicator)
        return saved.id ?: error("saved entity should have had an ID")
    }
}
