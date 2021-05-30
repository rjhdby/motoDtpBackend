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
    @Operation(tags = ["Accident API"], summary = "Fetch a list of accidents")
    @GetMapping(value = ["/list"])
    fun getList(
        @RequestHeader("token") token: String,
        @Parameter(description = "Fetch depth in hours") @RequestParam depth: Int,
        @RequestParam lat: Double?,
        @RequestParam lon: Double?,
        @RequestParam radius: Int?,
        @Parameter(description = "Last fetch time as UNIX timestamp in milliseconds") @RequestParam lastFetch: Long?
    ): Mono<ResponseEntity<List<AccidentResponse>>> = listResponse {
        accidentService.getList(
            token,
            depth,
            lastFetch,
            GeoConstraint.fromParams(lat, lon, radius)
        )
    }

    @Operation(tags = ["Accident API"], summary = "Get a single accident")
    @GetMapping(value = ["/{id}"])
    fun get(
        @RequestHeader("token") token: String,
        @PathVariable id: String
    ): Mono<ResponseEntity<AccidentResponse>> = response { accidentService.get(token, id) }

    @Operation(tags = ["Accident API"], summary = "Create new accident")
    @PostMapping(value = ["/"])
    fun create(
        @RequestHeader("token") token: String,
        @RequestBody request: CreateAccidentRequest
    ): Mono<ResponseEntity<AccidentResponse>> = response { accidentService.create(token, request) }

    @Operation(tags = ["Accident API"], summary = "Update an accident")
    @PostMapping(value = ["/{id}"])
    fun update(
        @RequestHeader("token") token: String,
        @PathVariable id: String,
        @RequestBody request: CreateAccidentRequest
    ): Mono<ResponseEntity<AccidentResponse>> = response { accidentService.update(token, id, request) }

    @Operation(tags = ["Accident API"], summary = "Hide an accident")
    @PutMapping(value = ["/{id}/hide"])
    fun hide(
        @RequestHeader("token") token: String,
        @PathVariable id: String,
    ): Mono<ResponseEntity<AccidentResponse>> = response { accidentService.setHidden(token, id, true) }

    @Operation(tags = ["Accident API"], summary = "Show previously hidden accident")
    @PutMapping(value = ["/{id}/show"])
    fun show(
        @RequestHeader("token") token: String,
        @PathVariable id: String,
    ): Mono<ResponseEntity<AccidentResponse>> = response { accidentService.setHidden(token, id, false) }

    @Operation(tags = ["Accident API"], summary = "Resolve an accident")
    @PutMapping(value = ["/{id}/resolve"])
    fun resolve(
        @RequestHeader("token") token: String,
        @PathVariable id: String,
    ): Mono<ResponseEntity<AccidentResponse>> = response { accidentService.setResolve(token, id, true) }

    @Operation(tags = ["Accident API"], summary = "Reopen an accident")
    @PutMapping(value = ["/{id}/reopen"])
    fun reopen(
        @RequestHeader("token") token: String,
        @PathVariable id: String,
    ): Mono<ResponseEntity<AccidentResponse>> = response { accidentService.setResolve(token, id, false) }

    @Operation(tags = ["Accident API"], summary = "Mark the accident as a conflict")
    @PutMapping(value = ["/{id}/conflict"])
    fun conflict(
        @RequestHeader("token") token: String,
        @PathVariable id: String,
    ): Mono<ResponseEntity<AccidentResponse>> = response { accidentService.setConflict(token, id, true) }

    @Operation(tags = ["Accident API"], summary = "Revoke a conflict mark")
    @PutMapping(value = ["/{id}/conflict/cancel"])
    fun cancelConflict(
        @RequestHeader("token") token: String,
        @PathVariable id: String,
    ): Mono<ResponseEntity<AccidentResponse>> = response { accidentService.setConflict(token, id, false) }

    private fun response(provider: suspend () -> Accident) =
        mono { handle { accidentConverter.toAccidentResponse(provider()) } }

    private fun listResponse(provider: suspend () -> Iterable<Accident>) =
        mono { handle { provider().map { accidentConverter.toAccidentResponse(it) } } }
}