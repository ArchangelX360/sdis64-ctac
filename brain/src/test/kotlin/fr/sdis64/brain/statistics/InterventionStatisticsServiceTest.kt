package fr.sdis64.brain.statistics

import fr.sdis64.brain.test.Query
import fr.sdis64.brain.test.integerSystelResponse
import fr.sdis64.brain.test.systelClientWithMockedHttp
import io.ktor.client.engine.mock.*
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InterventionStatisticsServiceTest {
    // FIXME: add failure tests as well

    @Test
    fun shouldGetInterventionStats() = runBlocking {
        val systelClient = systelClientWithMockedHttp {
            onDataSourceCall("cta1.sinistre.categorie.jour") {
                respondOk(
                    """{"result":[["AUTRES ACTIVITES",73],["ACCIDENT DE LA CIRCULATION",1]],"errors":[]}"""
                )
            }
            onDataSourceCall("cta1.inter.sinistre.categorie.en_cours") {
                respondOk(
                    """{"result":[["ACCIDENT DE LA CIRCULATION",2]],"errors":[]}"""
                )
            }
            onDataSourceCall(
                Query(
                    dataSource = "cta1.inter.annee.choix_categories",
                    category = "ACCIDENT DE LA CIRCULATION"
                )
            ) {
                respondOk(integerSystelResponse(3092))
            }
            onDataSourceCall(
                Query(
                    dataSource = "cta1.inter.annee.choix_categories",
                    category = "ACCIDENT NE NECESSITANT QUE DES SECOURS A VICTIMES"
                )
            ) {
                respondOk(integerSystelResponse(0))
            }
            onDataSourceCall(
                Query(
                    dataSource = "cta1.inter.annee.choix_categories",
                    category = "INCENDIE"
                )
            ) {
                respondOk(integerSystelResponse(0))
            }
            onDataSourceCall(
                Query(
                    dataSource = "cta1.inter.annee.choix_categories",
                    category = "RISQUES TECHNOLOGIQUES"
                )
            ) {
                respondOk(integerSystelResponse(0))
            }
            onDataSourceCall(
                Query(
                    dataSource = "cta1.inter.annee.choix_categories",
                    category = "OPERATIONS DIVERSES"
                )
            ) {
                respondOk(integerSystelResponse(0))
            }
        }

        val statisticsService = InterventionStatisticsService(
            InterventionStatisticsConfiguration(1000),
            systelClient,
            SimpleMeterRegistry(),
        )

        val actual = statisticsService.getInterventionStats()

        assertTrue(actual.contains("avp"))
        assertEquals("avp", actual["avp"]?.label)
        assertEquals("ACCIDENT DE LA CIRCULATION", actual["avp"]?.title)
        assertEquals(2, actual["avp"]?.ongoing)
        assertEquals(1, actual["avp"]?.day)
        assertEquals(3092, actual["avp"]?.year)
    }
}
