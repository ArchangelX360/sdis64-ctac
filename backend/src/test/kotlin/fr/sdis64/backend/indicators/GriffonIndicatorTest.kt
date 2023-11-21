package fr.sdis64.backend.indicators

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class GriffonIndicatorTest {

    @Test
    fun `should default`() {
        assertNotNull(GriffonIndicator.create(null))
        assertNotNull(GriffonIndicator.create("some unkown level"))
    }

    @Test
    fun `should get relevant indicator`() {
        assertEquals(GriffonIndicator.EXCEPTIONNEL, GriffonIndicator.create("Exceptionnel"))
    }
}
