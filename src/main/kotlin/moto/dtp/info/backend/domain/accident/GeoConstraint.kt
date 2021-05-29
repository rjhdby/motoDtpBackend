package moto.dtp.info.backend.domain.accident

data class GeoConstraint(val center: GeoPoint?, val radius: Int) {

    fun matches(geoPoint: GeoPoint): Boolean = when (center) {
        null -> true
        else -> center.distanceTo(geoPoint) < radius
    }

    companion object {
        fun fromParams(lat: Double?, lon: Double?, radius: Int? = null): GeoConstraint = when {
            lat == null || lon == null -> null
            else                       -> GeoPoint(lat, lon)
        }.let { GeoConstraint(it, radius ?: DEFAULT_RADIUS_KM) }

        fun fromAddress(address: Address, radius: Int? = null): GeoConstraint = fromParams(
            address.lat.toDouble(), address.lon.toDouble(), radius
        )

        private const val DEFAULT_RADIUS_KM = 10
    }
}
