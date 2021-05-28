package moto.dtp.info.backend.rest.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import kotlinx.coroutines.reactor.mono
import moto.dtp.info.backend.domain.accident.Accident
import moto.dtp.info.backend.domain.accident.GeoConstraint
import moto.dtp.info.backend.rest.Versions
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
    @Operation(tags = ["Accident API"], description = "Fetch a list of accidents")
    @GetMapping(value = ["/list"])
    fun getList(
        @RequestHeader("token") token: String,
        @Parameter(description = "Fetch depth in hours") @RequestParam depth: Int,
        @RequestParam lat: Double?,
        @RequestParam lon: Double?,
        @RequestParam radius: Int?,
        @Parameter(description = "Last fetch time as UNIX timestamp in milliseconds") @RequestParam lastFetch: Long?
    ): Mono<ResponseEntity<List<AccidentResponse>>> = mono {
        accidentService.getList(
            token,
            depth,
            lastFetch,
            GeoConstraint.fromParams(lat, lon, radius)
        ).toResponse()
    }

    @Operation(tags = ["Accident API"], description = "Get a single accident")
    @GetMapping(value = ["/{id}"])
    fun get(
        @RequestHeader("token") token: String,
        @PathVariable id: String
    ): Mono<ResponseEntity<AccidentResponse>> = mono { accidentService.get(token, id).toResponse() }

    @Operation(tags = ["Accident API"], description = "Create new accident")
    @PostMapping(value = ["/"])
    fun create(
        @RequestHeader("token") token: String,
        @RequestBody request: CreateAccidentRequest
    ): Mono<ResponseEntity<AccidentResponse>> =
        mono { accidentService.create(token, request).toResponse() }

    @Operation(tags = ["Accident API"], description = "Update an accident")
    @PostMapping(value = ["/{id}"])
    fun update(
        @RequestHeader("token") token: String,
        @PathVariable id: String,
        @RequestBody request: CreateAccidentRequest
    ): Mono<ResponseEntity<AccidentResponse>> =
        mono { accidentService.update(token, id, request).toResponse() }

    @Operation(tags = ["Accident API"], description = "Hide an accident")
    @PutMapping(value = ["/{id}/hide"])
    fun hide(
        @RequestHeader("token") token: String,
        @PathVariable id: String,
    ): Mono<ResponseEntity<AccidentResponse>> =
        mono { accidentService.setHidden(token, id, true).toResponse() }

    @Operation(tags = ["Accident API"], description = "Show previously hidden accident")
    @PutMapping(value = ["/{id}/show"])
    fun show(
        @RequestHeader("token") token: String,
        @PathVariable id: String,
    ): Mono<ResponseEntity<AccidentResponse>> =
        mono { accidentService.setHidden(token, id, false).toResponse() }

    @Operation(tags = ["Accident API"], description = "Resolve an accident")
    @PutMapping(value = ["/{id}/resolve"])
    fun resolve(
        @RequestHeader("token") token: String,
        @PathVariable id: String,
    ): Mono<ResponseEntity<AccidentResponse>> =
        mono { accidentService.setResolve(token, id, true).toResponse() }

    @Operation(tags = ["Accident API"], description = "Reopen an accident")
    @PutMapping(value = ["/{id}/reopen"])
    fun reopen(
        @RequestHeader("token") token: String,
        @PathVariable id: String,
    ): Mono<ResponseEntity<AccidentResponse>> =
        mono { accidentService.setResolve(token, id, false).toResponse() }

    @Operation(tags = ["Accident API"], description = "Mark the accident as a conflict")
    @PutMapping(value = ["/{id}/conflict"])
    fun conflict(
        @RequestHeader("token") token: String,
        @PathVariable id: String,
    ): Mono<ResponseEntity<AccidentResponse>> =
        mono { accidentService.setConflict(token, id, true).toResponse() }

    @Operation(tags = ["Accident API"], description = "Revoke a conflict mark")
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