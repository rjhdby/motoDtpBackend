package moto.dtp.info.backend.domain.accident

import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

data class GeoPoint(val lat: Double, val lon: Double) {
    private val radLat: Double by lazy { Math.toRadians(lat) }
    private val sinLat: Double by lazy { sin(radLat) }
    private val cosLat: Double by lazy { cos(radLat) }

    fun distanceTo(other: GeoPoint): Double {
        val theta = Math.toRadians(lon - other.lon)
        val dist = sinLat * other.sinLat + cosLat * other.cosLat * cos(theta)

        return Math.toDegrees(acos(dist)) * MAGIC_CONSTANT
    }

    companion object {
        private const val MAGIC_CONSTANT = 111.18957696
    }
}
