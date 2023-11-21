package fr.sdis64.backend.systel

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class SystelResponseTest {

    @Test
    fun `should parse integer response`() {
        val r = SystelResponse(listOf(listOf("3148")))
        assertEquals(3148, r.toInteger())
    }

    @Test
    fun `should fail to parse an empty integer response`() {
        val r = SystelResponse(listOf())
        val exception = assertThrows<IllegalArgumentException> { r.toInteger() }
        assertEquals(
            "SystelResponse result array must contain one single element, got 0",
            exception.message
        )
    }

    @Test
    fun `should fail to parse an integer response with an empty first element array`() {
        val r = SystelResponse(listOf(listOf()))
        val exception = assertThrows<IllegalArgumentException> { r.toInteger() }
        assertEquals(
            "SystelResponse first element of the result array must contain one single element, got 0",
            exception.message
        )
    }

    @Test
    fun `should fail to parse an invalid integer response`() {
        val r = SystelResponse(listOf(listOf("oops")))
        assertThrows<NumberFormatException> { r.toInteger() }
    }

    @Test
    fun `should fail to parse integer response as a map`() {
        val r = SystelResponse(listOf(listOf("3148")))
        val exception = assertThrows<IllegalArgumentException> { r.toMap() }
        assertEquals(
            "SystelResponse result element should contain two elements that represents a map entry, got 1 elements",
            exception.message
        )
    }

    @Test
    fun `should parse map response`() {
        val r = SystelResponse(listOf(listOf("INCENDIE", "123"), listOf("ACCIDENT DE LA CIRCULATION", "456")))
        assertEquals(
            mapOf(
                "INCENDIE" to 123,
                "ACCIDENT DE LA CIRCULATION" to 456,
            ), r.toMap()
        )
    }

    @Test
    fun `should parse empty map response`() {
        // this happens for example when there has been no interventions in the morning yet
        val r = SystelResponse(listOf())
        assertEquals(
            mapOf(), r.toMap()
        )
    }

    @Test
    fun `should fail to parse invalid map response (broken map entry)`() {
        val r = SystelResponse(listOf(listOf("INCENDIE", "123"), listOf("ACCIDENT DE LA CIRCULATION")))
        val exception = assertThrows<IllegalArgumentException> { r.toMap() }
        assertEquals(
            "SystelResponse result element should contain two elements that represents a map entry, got 1 elements",
            exception.message
        )
    }

    @Test
    fun `should fail to parse invalid map response (empty)`() {
        val r = SystelResponse(listOf(listOf("INCENDIE", "123"), listOf()))
        val exception = assertThrows<IllegalArgumentException> { r.toMap() }
        assertEquals(
            "SystelResponse result element should contain two elements that represents a map entry, got 0 elements",
            exception.message
        )
    }

    @Test
    fun `should fail to parse map response as integer`() {
        val r = SystelResponse(listOf(listOf("INCENDIE", "123"), listOf("ACCIDENT DE LA CIRCULATION", "456")))
        val exception = assertThrows<IllegalArgumentException> { r.toInteger() }
        assertEquals(
            "SystelResponse result array must contain one single element, got 2",
            exception.message
        )
    }

}
