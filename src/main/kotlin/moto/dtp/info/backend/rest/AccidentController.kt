package moto.dtp.info.backend.rest

import kotlinx.coroutines.reactor.mono
import moto.dtp.info.backend.domain.accident.Accident
import moto.dtp.info.backend.domain.accident.GeoConstraint
import moto.dtp.info.backend.rest.handler.ResponseHandler.handle
import moto.dtp.info.backend.rest.request.CreateAccidentRequest
import moto.dtp.info.backend.service.AccidentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("accident")
class AccidentController(
    private val accidentService: AccidentService,
) {
    @GetMapping(value = ["/list/"])
    fun getList(
        @RequestHeader("token") token: String,
        @RequestParam depth: Int,
        @RequestParam lat: Double?,
        @RequestParam lon: Double?,
        @RequestParam radius: Int?,
        @RequestParam lastFetch: Long?
    ): Mono<ResponseEntity<List<Accident>>> = mono {
        handle {
            accidentService.getList(
                token,
                depth,
                lastFetch,
                GeoConstraint.fromParams(lat, lon, radius)
            )
        }
    }

    @GetMapping(value = ["/{id}/"])
    fun get(
        @RequestHeader("token") token: String,
        @PathVariable id: String
    ): Mono<ResponseEntity<Accident>> = mono { handle { accidentService.get(token, id) } }

    @PostMapping(value = ["/"])
    fun create(
        @RequestHeader("token") token: String,
        @RequestBody request: CreateAccidentRequest
    ): Mono<ResponseEntity<Accident>> = mono { handle { accidentService.create(token, request) } }

    @PostMapping(value = ["/{id}"])
    fun update(
        @RequestHeader("token") token: String,
        @PathVariable id: String,
        @RequestBody request: CreateAccidentRequest
    ): Mono<ResponseEntity<Accident>> = mono { handle { accidentService.update(token, id, request) } }

    @PutMapping(value = ["/{id}/hide"])
    fun hide(
        @RequestHeader("token") token: String,
        @PathVariable id: String,
    ): Mono<ResponseEntity<Accident>> = mono { handle { accidentService.setHidden(token, id, true) } }

    @PutMapping(value = ["/{id}/show"])
    fun show(
        @RequestHeader("token") token: String,
        @PathVariable id: String,
    ): Mono<ResponseEntity<Accident>> = mono { handle { accidentService.setHidden(token, id, false) } }

    @PutMapping(value = ["/{id}/resolve"])
    fun resolve(
        @RequestHeader("authorization") token: String,
        @PathVariable id: String,
    ): Mono<ResponseEntity<Accident>> = mono { handle { accidentService.setResolve(token, id, true) } }

    @PutMapping(value = ["/{id}/reopen"])
    fun reopen(
        @RequestHeader("token") token: String,
        @PathVariable id: String,
    ): Mono<ResponseEntity<Accident>> = mono { handle { accidentService.setResolve(token, id, false) } }

    @PutMapping(value = ["/{id}/conflict"])
    fun conflict(
        @RequestHeader("token") token: String,
        @PathVariable id: String,
    ): Mono<ResponseEntity<Accident>> = mono { handle { accidentService.setConflict(token, id, true) } }

    @PutMapping(value = ["/{id}/conflict/cancel"])
    fun cancelConflict(
        @RequestHeader("token") token: String,
        @PathVariable id: String,
    ): Mono<ResponseEntity<Accident>> = mono { handle { accidentService.setConflict(token, id, false) } }
}