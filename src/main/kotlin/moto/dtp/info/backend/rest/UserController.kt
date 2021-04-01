package moto.dtp.info.backend.rest

import kotlinx.coroutines.reactor.mono
import moto.dtp.info.backend.rest.handler.ResponseHandler.handle
import moto.dtp.info.backend.rest.request.AuthRequest
import moto.dtp.info.backend.rest.response.UserResponse
import moto.dtp.info.backend.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("user")
class UserController(
    private val userService: UserService,
) {
    @GetMapping(value = ["/"])
    fun auth(
        @RequestHeader(value = "token") token: String
    ): Mono<ResponseEntity<UserResponse>> = mono { handle { userService.getUserResponse(token) } }

    @PostMapping(value = ["/register/anonymous"])
    fun registerAnonymous(): Mono<ResponseEntity<String>> =
        mono { handle { userService.register(AuthRequest.Anonymous) } }

    @PostMapping(value = ["/register/basic"], consumes = ["application/json"])
    fun registerBasic(
        @RequestBody request: AuthRequest.Basic
    ): Mono<ResponseEntity<String>> = mono { handle { userService.register(request) } }
}