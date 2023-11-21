package fr.sdis64.backend.statistics

import fr.sdis64.backend.test.integerSystelResponse
import fr.sdis64.backend.test.systelClientWithMockedHttp
import io.ktor.client.engine.mock.*
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ResponseTimeStatisticsServiceTest {
    // FIXME: add failure tests as well

    @Test
    fun shouldGetResponseTime() = runBlocking {
        val systelClient = systelClientWithMockedHttp {
            onDataSourceCall("appel.appel_urgent.sdis.attente_moyenne_sec") { respondOk(integerSystelResponse(20)) }
        }

        val statisticsService = ResponseTimeStatisticService(
            ResponseTimeStatisticConfiguration(1000),
            systelClient,
            SimpleMeterRegistry(),
        )

        val actual = statisticsService.getResponseTime()

        assertEquals(17, actual) // it should include the Systel response time error drift
    }
}
