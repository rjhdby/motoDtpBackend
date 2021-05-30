package moto.dtp.info.backend.rest.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import kotlinx.coroutines.reactor.mono
import moto.dtp.info.backend.domain.user.User
import moto.dtp.info.backend.rest.Versions
import moto.dtp.info.backend.rest.converter.UserConverter
import moto.dtp.info.backend.rest.handler.ResponseHandler.handle
import moto.dtp.info.backend.rest.request.AuthRequest
import moto.dtp.info.backend.rest.response.UserResponse
import moto.dtp.info.backend.service.UserService
import moto.dtp.info.backend.utils.ThrowUtils.throwInternal
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping(value = ["${Versions.V1}/user"])
class UserController(
    private val userService: UserService,
    private val userConverter: UserConverter
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @Operation(tags = ["User API"], summary = "Auth and retrieve user information")
    @GetMapping(value = ["/"])
    fun auth(
        @RequestHeader(value = "token") token: String
    ): Mono<ResponseEntity<UserResponse>> = response { userService.getUserByToken(token) }

    @Operation(tags = ["User API"], summary = "Register as anonymous")
    @PostMapping(value = ["/register/anonymous"])
    fun registerAnonymous(): Mono<ResponseEntity<String>> =
        mono { handle { userService.register(AuthRequest.Anonymous) } }

    @Operation(tags = ["User API"], summary = "Register with login and password")
    @PostMapping(value = ["/register/basic"], consumes = ["application/json"])
    fun registerBasic(
        @RequestBody request: AuthRequest.Basic
    ): Mono<ResponseEntity<String>> = mono { handle { userService.register(request) } }

    @Operation(tags = ["User API"], summary = "Register via VK OAuth. Callback for VK OAuth API.")
    @GetMapping(value = ["/register/vk"])
    fun registerVK(
        @RequestParam code: String?,
        @RequestParam error: String?,
        @RequestParam("error_description") description: String?
    ): Mono<ResponseEntity<String>> {
        if (code == null) {
            logger.error(error)
            logger.error(description)
            return mono { handle { throwInternal() } }
        }

        return mono { handle { userService.register(AuthRequest.VK(code)) } }
    }

    @Operation(tags = ["User API"], summary = "Invalidate user cache. For developer purposes only.")
    @GetMapping(value = ["/invalidate/{id}"])
    fun invalidateCache(
        @Parameter(description = "User ID") @RequestParam id: String
    ): Mono<ResponseEntity<UserResponse>> = response { userService.invalidate(id) }

    private fun response(provider: suspend () -> User) = mono { handle { userConverter.toUserResponse(provider()) } }
}