package fr.sdis64.backend.operators

import fr.sdis64.api.operators.Operator
import fr.sdis64.backend.operators.entities.OperatorPhoneNumber
import fr.sdis64.backend.operators.entities.OperatorStatus
import fr.sdis64.backend.test.configurations.TestDbConfiguration
import fr.sdis64.backend.test.systelClientWithMockedHttp
import fr.sdis64.backend.utilities.toDTO
import io.ktor.client.engine.mock.*
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.assertEquals

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
    classes = [
        OperatorStatusRepository::class,
        OperatorPhoneNumberRepository::class,
        TestDbConfiguration::class,
    ],
    initializers = [TestDbConfiguration.Initializer::class],
)
@ActiveProfiles("test")
@Testcontainers
class OperatorServiceTest {

    @Autowired
    private lateinit var operatorStatusRepository: OperatorStatusRepository

    @Autowired
    private lateinit var operatorPhoneNumberRepository: OperatorPhoneNumberRepository

    @AfterEach
    fun tearDown() {
        operatorPhoneNumberRepository.deleteAll()
        operatorStatusRepository.deleteAll()
    }

    @Test
    fun shouldGetOperators() = runBlocking {
        val systelClient = systelClientWithMockedHttp {
            onDataSourceCall("operateur.connecte.telephonie") {
                respondOk(
                    """{"result":[["systel","BEDIN","C23","CTA - Operateur","Test status"],["systel","BEDIN2","C25","ADMIN","Test status"]],"errors":[]}"""
                )
            }
        }

        val expectedOperatorPhoneNumber = operatorPhoneNumberRepository.save(
            OperatorPhoneNumber(
                systelNumber = "systel",
                realNumber = "real",
            )
        )

        val expectedOperatorStatus = operatorStatusRepository.save(
            OperatorStatus(
                name = "Test status",
                backgroundColor = "000000",
                textColor = "000000",
                displayed = true,
            )
        )

        val operatorService = OperatorService(
            systelClient,
            operatorStatusRepository,
            operatorPhoneNumberRepository,
            OperatorsConfiguration(1000),
            SimpleMeterRegistry(),
        )
        val actual = operatorService.getOperators()

        val expectedOperator1 = Operator(
            name = "BEDIN",
            post = "C23",
            function = "CTA - Operateur",
            phoneNumber = expectedOperatorPhoneNumber.toDTO(),
            status = expectedOperatorStatus.toDTO(),
        )
        val expectedOperator2 = Operator(
            name = "BEDIN2",
            post = "C25",
            function = "ADMIN",
            phoneNumber = expectedOperatorPhoneNumber.toDTO(),
            status = expectedOperatorStatus.toDTO(),
        )
        val expectedOperators = setOf(expectedOperator1, expectedOperator2)

        assertEquals(2, actual.size)
        assertEquals(expectedOperators, actual)
    }
}
