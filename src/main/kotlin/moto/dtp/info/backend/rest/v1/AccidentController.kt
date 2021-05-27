package moto.dtp.info.backend.rest.v1

import kotlinx.coroutines.reactor.mono
import moto.dtp.info.backend.domain.accident.Accident
import moto.dtp.info.backend.domain.accident.GeoConstraint
import moto.dtp.info.backend.rest.converter.AccidentConverter
import moto.dtp.info.backend.rest.handler.ResponseHandler.handle
import moto.dtp.info.backend.rest.request.CreateAccidentRequest
import moto.dtp.info.backend.rest.response.AccidentResponse
import moto.dtp.info.backend.service.AccidentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping(value = ["${Versions.V1}/accident"])
class AccidentController(
    private val accidentService: AccidentService,
    private val accidentConverter: AccidentConverter
) {
    @GetMapping(value = ["/list"])
    fun getList(
        @RequestHeader("token") token: String,
        @RequestParam depth: Int,
        @RequestParam lat: Double?,
        @RequestParam lon: Double?,
        @RequestParam radius: Int?,
        @RequestParam lastFetch: Long?
    ): Mono<ResponseEntity<List<AccidentResponse>>> = mono {
        accidentService.getList(
            token,
            depth,
            lastFetch,
            GeoConstraint.fromParams(lat, lon, radius)
        ).toResponse()
    }

    @GetMapping(value = ["/{id}"])
    fun get(
        @RequestHeader("token") token: String,
        @PathVariable id: String
    ): Mono<ResponseEntity<AccidentResponse>> = mono { accidentService.get(token, id).toResponse() }

    @PostMapping(value = ["/"])
    fun create(
        @RequestHeader("token") token: String,
        @RequestBody request: CreateAccidentRequest
    ): Mono<ResponseEntity<AccidentResponse>> =
        mono { accidentService.create(token, request).toResponse() }

    @PostMapping(value = ["/{id}"])
    fun update(
        @RequestHeader("token") token: String,
        @PathVariable id: String,
        @RequestBody request: CreateAccidentRequest
    ): Mono<ResponseEntity<AccidentResponse>> =
        mono { accidentService.update(token, id, request).toResponse() }

    @PutMapping(value = ["/{id}/hide"])
    fun hide(
        @RequestHeader("token") token: String,
        @PathVariable id: String,
    ): Mono<ResponseEntity<AccidentResponse>> =
        mono { accidentService.setHidden(token, id, true).toResponse() }

    @PutMapping(value = ["/{id}/show"])
    fun show(
        @RequestHeader("token") token: String,
        @PathVariable id: String,
    ): Mono<ResponseEntity<AccidentResponse>> =
        mono { accidentService.setHidden(token, id, false).toResponse() }

    @PutMapping(value = ["/{id}/resolve"])
    fun resolve(
        @RequestHeader("token") token: String,
        @PathVariable id: String,
    ): Mono<ResponseEntity<AccidentResponse>> =
        mono { accidentService.setResolve(token, id, true).toResponse() }

    @PutMapping(value = ["/{id}/reopen"])
    fun reopen(
        @RequestHeader("token") token: String,
        @PathVariable id: String,
    ): Mono<ResponseEntity<AccidentResponse>> =
        mono { accidentService.setResolve(token, id, false).toResponse() }

    @PutMapping(value = ["/{id}/conflict"])
    fun conflict(
        @RequestHeader("token") token: String,
        @PathVariable id: String,
    ): Mono<ResponseEntity<AccidentResponse>> =
        mono { accidentService.setConflict(token, id, true).toResponse() }

    @PutMapping(value = ["/{id}/conflict/cancel"])
    fun cancelConflict(
        @RequestHeader("token") token: String,
        @PathVariable id: String,
    ): Mono<ResponseEntity<AccidentResponse>> =
        mono { accidentService.setConflict(token, id, false).toResponse() }

    private suspend fun Accident.toResponse() = handle {
        accidentConverter.toAccidentResponse(this)
    }

    private suspend fun Iterable<Accident>.toResponse() = handle {
        map { accidentConverter.toAccidentResponse(it) }
    }
}