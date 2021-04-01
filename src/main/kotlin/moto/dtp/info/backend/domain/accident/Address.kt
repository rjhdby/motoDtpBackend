package moto.dtp.info.backend.domain.accident

import com.fasterxml.jackson.annotation.JsonIgnore

data class Address(val lat: Float, val lon: Float, val address: String) {
    @JsonIgnore
    fun getGeoPoint() = GeoPoint(lat.toDouble(), lon.toDouble())
}
