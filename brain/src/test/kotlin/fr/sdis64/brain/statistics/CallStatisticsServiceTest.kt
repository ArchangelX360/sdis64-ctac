package fr.sdis64.brain.statistics

import fr.sdis64.brain.test.integerSystelResponse
import fr.sdis64.brain.test.systelClientWithMockedHttp
import io.ktor.client.engine.mock.*
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class CallStatisticsServiceTest {
    // FIXME: add failure tests as well

    @Test
    fun shouldGetCallStats() = runBlocking {
        val systelClient = systelClientWithMockedHttp {
            onDataSourceCall("cta1.appel.18.plancher_7h") { respondOk(integerSystelResponse(10)) }
            onDataSourceCall("cta1.appel.18.en_cours") { respondOk(integerSystelResponse(2)) }
            onDataSourceCall("cta1.appel.18.entrants") { respondOk(integerSystelResponse(1)) }
            onDataSourceCall("cta1.appel.18.annee") { respondOk(integerSystelResponse(100)) }

            onDataSourceCall("cta1.appel.operationnel.jour") { respondOk(integerSystelResponse(0)) }
            onDataSourceCall("cta1.appel.operationnel.annee") { respondOk(integerSystelResponse(0)) }
            onDataSourceCall("cta1.appel.operationnel.en_cours") { respondOk(integerSystelResponse(0)) }
            onDataSourceCall("cta1.appel.operationnel.en_attente") { respondOk(integerSystelResponse(0)) }

            onDataSourceCall("cta1.appel.sortant.jour") { respondOk(integerSystelResponse(0)) }
            onDataSourceCall("cta1.appel.sortant.annee") { respondOk(integerSystelResponse(0)) }
            onDataSourceCall("cta1.appel.sortant.en_cours") { respondOk(integerSystelResponse(0)) }

            onDataSourceCall("cta1.appel.entrant.sortant.jour") { respondOk(integerSystelResponse(20)) }
            onDataSourceCall("cta1.appel.entrant.sortant.annee") { respondOk(integerSystelResponse(200)) }
            onDataSourceCall("cta1.appel.entrant.sortant.en_cours") { respondOk(integerSystelResponse(4)) }

            onDataSourceCall("cta1.appel.ecobuage.jour") { respondOk(integerSystelResponse(0)) }
            onDataSourceCall("cta1.appel.ecobuage.annee") { respondOk(integerSystelResponse(0)) }
            onDataSourceCall("cta1.appel.ecobuage.en_cours") { respondOk(integerSystelResponse(0)) }
            onDataSourceCall("cta1.appel.ecobuage.en_attente") { respondOk(integerSystelResponse(0)) }
        }

        val statisticsService = CallStatisticsService(
            CallsConfiguration(1000),
            systelClient,
            SimpleMeterRegistry(),
        )

        val actual = statisticsService.getCallStats()

        assertFalse(actual.isEmpty())

        assertEquals(10, actual["emergency"]?.day)
        assertEquals(2, actual["emergency"]?.ongoing)
        assertEquals(100, actual["emergency"]?.year)
        assertEquals(1, actual["emergency"]?.onhold)
        assertEquals("emergency", actual["emergency"]?.label)
        assertEquals("18", actual["emergency"]?.title)

        assertEquals(20, actual["inOut"]?.day)
        assertEquals(4, actual["inOut"]?.ongoing)
        assertEquals(200, actual["inOut"]?.year)
        assertEquals(null, actual["inOut"]?.onhold)
        assertEquals("inOut", actual["inOut"]?.label)
        assertEquals("Entrants/Sortants", actual["inOut"]?.title)
    }
}
