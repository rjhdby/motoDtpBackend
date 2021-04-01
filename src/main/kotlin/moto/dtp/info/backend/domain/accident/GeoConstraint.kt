package moto.dtp.info.backend.domain.accident

data class GeoConstraint(val center: GeoPoint?, val radius: Int) {

    fun matches(geoPoint: GeoPoint): Boolean = when (center) {
        null -> true
        else -> center.distanceTo(geoPoint) < radius
    }

    companion object {
        fun fromParams(lat: Double?, lon: Double?, radius: Int?): GeoConstraint = when {
            lat == null || lon == null -> null
            else                       -> GeoPoint(lat, lon)
        }.let { GeoConstraint(it, radius ?: DEFAULT_RADIUS_KM) }

        private const val DEFAULT_RADIUS_KM = 10
    }
}
