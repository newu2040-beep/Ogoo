package com.example.data.model

data class HistogramData(
    val redChannel: FloatArray = FloatArray(256),
    val greenChannel: FloatArray = FloatArray(256),
    val blueChannel: FloatArray = FloatArray(256),
    val lumChannel: FloatArray = FloatArray(256)
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as HistogramData
        return redChannel.contentEquals(other.redChannel) &&
                greenChannel.contentEquals(other.greenChannel) &&
                blueChannel.contentEquals(other.blueChannel)
    }

    override fun hashCode(): Int {
        var result = redChannel.contentHashCode()
        result = 31 * result + greenChannel.contentHashCode()
        result = 31 * result + blueChannel.contentHashCode()
        return result
    }
}
