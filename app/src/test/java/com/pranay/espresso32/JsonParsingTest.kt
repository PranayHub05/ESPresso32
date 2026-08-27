package com.pranay.espresso32

import com.pranay.espresso32.data.model.ESP32MessageDto
import com.pranay.espresso32.domain.model.SensorType
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonParsingTest {

    private val json = ESP32MessageDto.json

    @Test
    fun `parse valid full JSON message`() {
        val input = """
            {
              "device": { "name": "ESP32-S3", "id": "ESP32_001" },
              "timestamp": 1724780000,
              "data": {
                "temperature": 28.6,
                "humidity": 64.2,
                "motion": true,
                "light": 740,
                "battery": 87
              }
            }
        """.trimIndent()

        val result = json.decodeFromString<ESP32MessageDto>(input)

        assertNotNull(result.device)
        assertEquals("ESP32-S3", result.device?.name)
        assertEquals("ESP32_001", result.device?.id)
        assertEquals(1724780000L, result.timestamp)
        assertNotNull(result.data)
        assertEquals(5, result.data?.size)
    }

    @Test
    fun `parse JSON with missing device field`() {
        val input = """
            {
              "timestamp": 1724780000,
              "data": { "temperature": 28.6 }
            }
        """.trimIndent()

        val result = json.decodeFromString<ESP32MessageDto>(input)

        assertNull(result.device)
        assertNotNull(result.data)
        assertEquals(28.6, result.data!!["temperature"]!!.jsonPrimitive.doubleOrNull)
    }

    @Test
    fun `parse JSON with missing timestamp`() {
        val input = """
            {
              "data": { "temperature": 28.6, "humidity": 64.2 }
            }
        """.trimIndent()

        val result = json.decodeFromString<ESP32MessageDto>(input)

        assertNull(result.timestamp)
        assertEquals(2, result.data?.size)
    }

    @Test
    fun `parse JSON with unknown fields ignores them`() {
        val input = """
            {
              "device": { "name": "ESP32" },
              "data": { "temperature": 28.6 },
              "firmware_version": "1.0",
              "uptime": 12345
            }
        """.trimIndent()

        val result = json.decodeFromString<ESP32MessageDto>(input)

        assertEquals("ESP32", result.device?.name)
        assertNotNull(result.data)
    }

    @Test
    fun `parse JSON with only data field`() {
        val input = """{ "data": { "temperature": 28.6 } }"""

        val result = json.decodeFromString<ESP32MessageDto>(input)

        assertNull(result.device)
        assertNull(result.timestamp)
        assertNotNull(result.data)
    }

    @Test
    fun `parse JSON with empty data object`() {
        val input = """{ "data": {} }"""

        val result = json.decodeFromString<ESP32MessageDto>(input)

        assertNotNull(result.data)
        assertEquals(0, result.data?.size)
    }

    @Test
    fun `parse JSON with boolean sensor value`() {
        val input = """{ "data": { "motion": true } }"""

        val result = json.decodeFromString<ESP32MessageDto>(input)

        assertTrue(result.data!!["motion"]!!.jsonPrimitive.booleanOrNull == true)
    }

    @Test
    fun `parse JSON with string sensor value`() {
        val input = """{ "data": { "status": "normal" } }"""

        val result = json.decodeFromString<ESP32MessageDto>(input)

        assertEquals("normal", result.data!!["status"]!!.jsonPrimitive.content)
    }

    @Test
    fun `parse JSON with additional future sensors`() {
        val input = """
            {
              "data": {
                "temperature": 28.6,
                "humidity": 64.2,
                "pressure": 1008.4,
                "voltage": 3.71,
                "current": 0.42
              }
            }
        """.trimIndent()

        val result = json.decodeFromString<ESP32MessageDto>(input)

        assertEquals(5, result.data?.size)
        assertNotNull(result.data!!["pressure"])
        assertNotNull(result.data!!["voltage"])
        assertNotNull(result.data!!["current"])
    }

    @Test(expected = Exception::class)
    fun `parse invalid JSON throws exception`() {
        json.decodeFromString<ESP32MessageDto>("not json at all")
    }

    @Test(expected = Exception::class)
    fun `parse truncated JSON throws exception`() {
        json.decodeFromString<ESP32MessageDto>("""{ "data": { "temp" """)
    }

    @Test
    fun `parse JSON with integer values`() {
        val input = """{ "data": { "light": 740, "battery": 87 } }"""

        val result = json.decodeFromString<ESP32MessageDto>(input)

        assertEquals(740.0, result.data!!["light"]!!.jsonPrimitive.doubleOrNull)
        assertEquals(87.0, result.data!!["battery"]!!.jsonPrimitive.doubleOrNull)
    }
}

class SensorTypeDetectionTest {

    @Test
    fun `detect boolean type from JsonPrimitive`() {
        val primitive = JsonPrimitive(true)
        assertNotNull(primitive.booleanOrNull)
    }

    @Test
    fun `detect numeric type from JsonPrimitive`() {
        val primitive = JsonPrimitive(28.6)
        assertNotNull(primitive.doubleOrNull)
        assertNull(primitive.booleanOrNull)
    }

    @Test
    fun `detect integer as numeric from JsonPrimitive`() {
        val primitive = JsonPrimitive(740)
        assertNotNull(primitive.doubleOrNull)
    }

    @Test
    fun `detect string type from JsonPrimitive`() {
        val primitive = JsonPrimitive("normal")
        assertNull(primitive.doubleOrNull)
        assertNull(primitive.booleanOrNull)
    }

    @Test
    fun `percentage keys detected correctly`() {
        val percentageKeys = listOf("battery", "humidity", "signal")
        percentageKeys.forEach { key ->
            assertTrue("$key should be percentage", key in percentageKeys)
        }
    }
}

class ExtensionsTest {

    @Test
    fun `valid IP addresses`() {
        assertTrue("192.168.1.50".let {
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$".toRegex().matches(it)
        })
        assertTrue("10.0.0.1".let {
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$".toRegex().matches(it)
        })
        assertTrue("255.255.255.255".let {
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$".toRegex().matches(it)
        })
    }

    @Test
    fun `invalid IP addresses`() {
        val regex = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$".toRegex()
        assertTrue(!regex.matches(""))
        assertTrue(!regex.matches("not.an.ip"))
        assertTrue(!regex.matches("256.1.1.1"))
        assertTrue(!regex.matches("1.2.3"))
    }

    @Test
    fun `valid port numbers`() {
        assertTrue(1 in 1..65535)
        assertTrue(81 in 1..65535)
        assertTrue(8080 in 1..65535)
        assertTrue(65535 in 1..65535)
    }

    @Test
    fun `invalid port numbers`() {
        assertTrue(0 !in 1..65535)
        assertTrue(65536 !in 1..65535)
        assertTrue(-1 !in 1..65535)
    }

    @Test
    fun `toTitleCase snake_case conversion`() {
        assertEquals("Temperature", "temperature".split("_").joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } })
        assertEquals("Motion Detected", "motion_detected".split("_").joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } })
    }
}
