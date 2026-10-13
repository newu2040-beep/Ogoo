package com.example.data.model

enum class ExportResolution(val label: String, val maxDimension: Int) {
    RES_720P("720p", 1280),
    RES_1080P("1080p", 1920),
    RES_4K("4K", 3840),
    RES_8K("8K", 7680),
    CUSTOM("Custom", 0)
}

enum class ExportFormat(val extension: String, val mimeType: String) {
    JPG("jpg", "image/jpeg"),
    PNG("png", "image/png"),
    WEBP("webp", "image/webp")
}

enum class ExportDestination {
    DEVICE,
    GALLERY
}

data class ExportConfig(
    val resolution: ExportResolution = ExportResolution.RES_8K,
    val customWidth: Int = 3840,
    val customHeight: Int = 2160,
    val quality: Int = 100, // 10 to 100%
    val format: ExportFormat = ExportFormat.JPG,
    val destination: ExportDestination = ExportDestination.DEVICE,
    val preserveExif: Boolean = true,
    val removeLocation: Boolean = false
) {
    fun estimatedSizeBytes(inputWidth: Int, inputHeight: Int): Long {
        val targetMax = if (resolution == ExportResolution.CUSTOM) {
            maxOf(customWidth, customHeight)
        } else {
            resolution.maxDimension
        }
        val scale = if (targetMax > 0 && maxOf(inputWidth, inputHeight) > targetMax) {
            targetMax.toDouble() / maxOf(inputWidth, inputHeight)
        } else 1.0

        val w = (inputWidth * scale).coerceAtLeast(100.0)
        val h = (inputHeight * scale).coerceAtLeast(100.0)
        val totalPixels = w * h

        return when (format) {
            ExportFormat.PNG -> (totalPixels * 2.8).toLong()
            ExportFormat.JPG -> (totalPixels * 0.35 * (quality / 100.0)).toLong()
            ExportFormat.WEBP -> (totalPixels * 0.22 * (quality / 100.0)).toLong()
        }
    }
}
