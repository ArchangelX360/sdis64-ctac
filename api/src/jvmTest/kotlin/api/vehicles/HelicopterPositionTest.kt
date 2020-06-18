package fr.sdis64.api.vehicles

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json as KxJson

@OptIn(ExperimentalSerializationApi::class)
internal class HelicopterPositionTest {

    @Test
    fun shouldParseHelicopterPosition() {
        val hP = HelicopterPosition.Known("1", "D64", 23F, 25F)
        val json = KxJson {
            encodeDefaults = true
            explicitNulls = false
        }
        val s = json.encodeToString(hP)
        assertEquals("""{"id":"1","alias":"D64","latitude":23.0,"longitude":25.0}""", s)
    }
}
