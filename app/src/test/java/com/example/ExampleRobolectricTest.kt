package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.EditRecipe
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("OGOO", appName)
    }

    @Test
    fun `editRecipe json roundtrip in Robolectric`() {
        val original = EditRecipe(
            exposure = 0.5f,
            contrast = 25f,
            highlights = -15f,
            shadows = 30f,
            temperature = 10f,
            tint = -5f,
            vibrance = 20f,
            saturation = 15f,
            lutId = "fuji_pro_400h",
            lutIntensity = 0.85f,
            rotationDegrees = 90f,
            flipHorizontal = true,
            cropAspect = "16:9"
        )

        val json = original.toJson()
        val restored = EditRecipe.fromJson(json)

        assertEquals(original.exposure, restored.exposure, 0.001f)
        assertEquals(original.contrast, restored.contrast, 0.001f)
        assertEquals(original.highlights, restored.highlights, 0.001f)
        assertEquals(original.shadows, restored.shadows, 0.001f)
        assertEquals(original.temperature, restored.temperature, 0.001f)
        assertEquals(original.lutId, restored.lutId)
        assertEquals(original.lutIntensity, restored.lutIntensity, 0.001f)
        assertEquals(original.rotationDegrees, restored.rotationDegrees, 0.001f)
        assertEquals(original.flipHorizontal, restored.flipHorizontal)
        assertEquals(original.cropAspect, restored.cropAspect)
    }
}
