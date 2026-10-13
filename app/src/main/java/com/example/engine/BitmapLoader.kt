package com.example.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import kotlin.math.max

object BitmapLoader {

    suspend fun loadBitmap(
        context: Context,
        uriString: String,
        sampleResName: String? = null,
        maxDimension: Int = 2048
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            if (sampleResName != null || uriString.startsWith("res://")) {
                val resName = sampleResName ?: uriString.removePrefix("res://")
                val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
                if (resId != 0) {
                    return@withContext decodeSampledResource(context, resId, maxDimension)
                }
            }

            val uri = Uri.parse(uriString)
            decodeSampledUri(context, uri, maxDimension)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun decodeSampledResource(context: Context, resId: Int, maxDimension: Int): Bitmap? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeResource(context.resources, resId, options)

        val srcMax = max(options.outWidth, options.outHeight)
        var sampleSize = 1
        while (srcMax / (sampleSize * 2) >= maxDimension) {
            sampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return BitmapFactory.decodeResource(context.resources, resId, decodeOptions)
    }

    private fun decodeSampledUri(context: Context, uri: Uri, maxDimension: Int): Bitmap? {
        var inputStream: InputStream? = context.contentResolver.openInputStream(uri) ?: return null
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(inputStream, null, options)
        inputStream?.close()

        val srcMax = max(options.outWidth, options.outHeight)
        var sampleSize = 1
        while (srcMax / (sampleSize * 2) >= maxDimension) {
            sampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream, null, decodeOptions)
        inputStream?.close()
        return bitmap
    }
}
