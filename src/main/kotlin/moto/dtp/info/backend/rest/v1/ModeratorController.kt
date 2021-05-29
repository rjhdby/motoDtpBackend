package moto.dtp.info.backend.rest.v1

import io.swagger.v3.oas.annotations.Operation
import kotlinx.coroutines.reactor.mono
import moto.dtp.info.backend.domain.user.User
import moto.dtp.info.backend.domain.user.UserRole
import moto.dtp.info.backend.rest.Versions
import moto.dtp.info.backend.rest.converter.UserConverter
import moto.dtp.info.backend.rest.handler.ResponseHandler.handle
import moto.dtp.info.backend.rest.response.UserResponse
import moto.dtp.info.backend.service.ModeratorService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping(value = ["${Versions.V1}/moderator"])
class ModeratorController(
    private val moderatorService: ModeratorService,
    private val userConverter: UserConverter
) {
    @Operation(tags = ["Moderator API"], summary = "Grant MODERATOR role to the user")
    @PutMapping(value = ["/{uid}/promote"])
    fun promoteModerator(
        @RequestHeader("token") token: String,
        @PathVariable uid: String
    ): Mono<ResponseEntity<UserResponse>> =
        mono { moderatorService.setRole(token, uid, UserRole.MODERATOR).toResponse() }

    @Operation(tags = ["Moderator API"], summary = "Revoke MODERATOR role from the user")
    @PutMapping(value = ["/{uid}/revoke"])
    fun revokeModerator(
        @RequestHeader("token") token: String,
        @PathVariable uid: String
    ): Mono<ResponseEntity<UserResponse>> = mono { moderatorService.setRole(token, uid, UserRole.USER).toResponse() }

    @Operation(tags = ["Moderator API"], summary = "Ban user")
    @PutMapping(value = ["/{uid}/ban"])
    fun ban(
        @RequestHeader("token") token: String,
        @PathVariable uid: String
    ): Mono<ResponseEntity<UserResponse>> =
        mono { moderatorService.setRole(token, uid, UserRole.READ_ONLY).toResponse() }

    @Operation(tags = ["Moderator API"], summary = "Unban user")
    @PutMapping(value = ["/{uid}/unban"])
    fun unban(
        @RequestHeader("token") token: String,
        @PathVariable uid: String
    ): Mono<ResponseEntity<UserResponse>> = mono { moderatorService.setRole(token, uid, UserRole.USER).toResponse() }

    private suspend fun User.toResponse() = handle { userConverter.toUserResponse(this) }
}