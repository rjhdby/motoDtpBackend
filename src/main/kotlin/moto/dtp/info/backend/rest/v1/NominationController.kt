package moto.dtp.info.backend.rest.v1

import io.swagger.v3.oas.annotations.Operation
import kotlinx.coroutines.reactor.mono
import moto.dtp.info.backend.domain.accident.GeoPoint
import moto.dtp.info.backend.rest.Versions
import moto.dtp.info.backend.rest.converter.NominationConverter
import moto.dtp.info.backend.rest.handler.ResponseHandler.handle
import moto.dtp.info.backend.service.NominationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping(value = ["${Versions.V1}/nomination"])
class NominationController(
    private val nominationService: NominationService,
    private val nominationConverter: NominationConverter
) {
    @Operation(tags = ["Nomination API"], summary = "Reverse geocoding")
    @GetMapping("/")
    fun get(
        @RequestHeader("token") token: String,
        @RequestParam lat: Double,
        @RequestParam lon: Double
    ): Mono<ResponseEntity<String>> {
        val geoPoint = GeoPoint(lat, lon)
        nominationService.addJob(token, geoPoint)
        return mono {
            handle {
                nominationConverter.toNominationResponse(nominationService.retrieveResultAsync(geoPoint).await())
            }
        }
    }
}