package com.example

import com.example.data.lut.CubeLutParser
import com.example.data.model.ExportConfig
import com.example.data.model.ExportFormat
import com.example.data.model.ExportResolution
import com.example.data.model.PresetCatalog
import com.example.engine.CurveEvaluator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class ExampleUnitTest {

    @Test
    fun presetCatalog_containsAtLeast110Presets() {
        val count = PresetCatalog.bundledPresets.size
        assertTrue("Preset count must be at least 110 (current: $count)", count >= 110)

        // Ensure distinct IDs
        val uniqueIds = PresetCatalog.bundledPresets.map { it.id }.toSet()
        assertEquals("All presets must have unique IDs", count, uniqueIds.size)
    }

    @Test
    fun curveEvaluator_buildLut_has256Entries() {
        val points = listOf(
            0f to 0f,
            0.5f to 0.7f,
            1f to 1f
        )
        val lut = CurveEvaluator.buildLut(points)
        assertEquals(256, lut.size)
        assertEquals(0, lut[0])
        assertEquals(255, lut[255])
        assertTrue("Midpoint should be boosted", lut[128] > 128)
    }

    @Test
    fun cubeLutParser_parsesValidCubeData() {
        val cubeContent = """
            # Test Cube file
            TITLE "Test 3D LUT"
            LUT_3D_SIZE 2
            0.0 0.0 0.0
            1.0 0.0 0.0
            0.0 1.0 0.0
            1.0 1.0 0.0
            0.0 0.0 1.0
            1.0 0.0 1.0
            0.0 1.0 1.0
            1.0 1.0 1.0
        """.trimIndent()

        val parsed = CubeLutParser.parse(ByteArrayInputStream(cubeContent.toByteArray()))
        assertNotNull(parsed)
        assertEquals("Test 3D LUT", parsed?.title)
        assertEquals(2, parsed?.size)
        assertEquals(24, parsed?.table?.size)

        // Test sample
        val sample = parsed?.sample3D(0f, 0f, 0f)
        assertEquals(0f, sample?.first ?: -1f, 0.01f)
    }

    @Test
    fun exportConfig_calculatesPositiveEstimatedSize() {
        val config = ExportConfig(
            resolution = ExportResolution.RES_4K,
            quality = 95,
            format = ExportFormat.JPG
        )
        val size = config.estimatedSizeBytes(3840, 2160)
        assertTrue("Estimated size must be positive", size > 0)
    }
}
