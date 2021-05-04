package moto.dtp.info.backend.rest.v1

import kotlinx.coroutines.reactor.mono
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
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

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
}