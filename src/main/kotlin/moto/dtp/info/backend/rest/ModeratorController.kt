package moto.dtp.info.backend.rest

import kotlinx.coroutines.reactor.mono
import moto.dtp.info.backend.domain.user.UserRole
import moto.dtp.info.backend.rest.handler.ResponseHandler.handle
import moto.dtp.info.backend.rest.response.UserResponse
import moto.dtp.info.backend.service.ModeratorService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("moderator")
class ModeratorController(
    private val moderatorService: ModeratorService,
) {
    @PutMapping(value = ["/{uid}/promote"])
    fun promoteModerator(
        @RequestHeader("token") token: String,
        @PathVariable uid: String
    ): Mono<ResponseEntity<UserResponse>> = mono { handle { moderatorService.setRole(token, uid, UserRole.MODERATOR) } }

    @PutMapping(value = ["/{uid}/revoke"])
    fun revokeModerator(
        @RequestHeader("token") token: String,
        @PathVariable uid: String
    ): Mono<ResponseEntity<UserResponse>> = mono { handle { moderatorService.setRole(token, uid, UserRole.USER) } }

    @PutMapping(value = ["/{uid}/ban"])
    fun ban(
        @RequestHeader("token") token: String,
        @PathVariable uid: String
    ): Mono<ResponseEntity<UserResponse>> = mono { handle { moderatorService.setRole(token, uid, UserRole.READ_ONLY) } }

    @PutMapping(value = ["/{uid}/unban"])
    fun unban(
        @RequestHeader("token") token: String,
        @PathVariable uid: String
    ): Mono<ResponseEntity<UserResponse>> = mono { handle { moderatorService.setRole(token, uid, UserRole.USER) } }
}