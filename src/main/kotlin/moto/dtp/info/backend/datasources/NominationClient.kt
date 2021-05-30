package moto.dtp.info.backend.datasources

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import moto.dtp.info.backend.configuration.MotoDtpConfiguration
import moto.dtp.info.backend.domain.accident.GeoPoint
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject

@Service
class NominationClient(
    configuration: MotoDtpConfiguration
) {
    private val url = configuration.nomination.url
    private val template = RestTemplate()

    suspend fun resolve(geoPoint: GeoPoint): NominationResult {
        return template.getForObject("$url&lon=${geoPoint.lon}&lat=${geoPoint.lat}")
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class NominationResult(
        val address: HashMap<String, String>
    )
}