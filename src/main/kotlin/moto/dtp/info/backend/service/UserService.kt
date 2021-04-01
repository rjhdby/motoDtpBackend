package moto.dtp.info.backend.service

import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import moto.dtp.info.backend.domain.user.*
import moto.dtp.info.backend.exception.NotFoundException
import moto.dtp.info.backend.repository.AuthRepository
import moto.dtp.info.backend.repository.UserRepository
import moto.dtp.info.backend.rest.request.AuthRequest
import moto.dtp.info.backend.rest.response.UserResponse
import org.springframework.stereotype.Service

@Service
class UserService(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val authRequestValidator: AuthRequestValidator,
    private val tokenService: TokenService,
) {
    suspend fun getUser(token: String): User? = when {
        tokenService.isAnonymous(token) -> ANONYMOUS_USER
        else                            -> userRepository.findByToken(token).awaitFirstOrNull()
    }

    suspend fun getUserResponse(token: String): UserResponse = UserResponse.fromUser(
        getUser(token) ?: throw NotFoundException()
    )

    suspend fun register(request: AuthRequest): String {
        authRequestValidator.validate(request)
        tryToLogin(request)?.let { return it }

        return when (request) {
            AuthRequest.Anonymous -> TokenService.ANONYMOUS_TOKEN
            is AuthRequest.Basic  -> registerBasic(request)
        }
    }

    private suspend fun tryToLogin(request: AuthRequest): String? {
        val auth = when (request) {
            AuthRequest.Anonymous -> return TokenService.ANONYMOUS_TOKEN
            is AuthRequest.Basic  -> authRepository.findByLogin(request.login).awaitFirstOrNull() ?: return null
        }

        return login(request, auth)
    }

    private suspend fun registerBasic(request: AuthRequest.Basic): String {
        val token = tokenService.createToken(request)
        val user = userRepository.save(User(token = token, nick = request.nick ?: "Unknown")).awaitFirst()
        val auth = Auth(
            uid = user.id!!,
            type = AuthType.BASIC,
            credentials = Credentials.fromRequest(request),
        )

        authRepository.save(auth).awaitFirst()

        return token
    }

    private suspend fun login(request: AuthRequest, auth: Auth): String {
        when (request) {
            is AuthRequest.Basic  -> auth.credentials?.guardPassword(request.password) ?: throwInternal()
            AuthRequest.Anonymous -> Unit
        }

        return userRepository.findById(auth.uid).awaitFirstOrNull()?.token ?: throwInternal()
    }

    private fun throwInternal(): Nothing {
        throw InternalError("Something went terrible wrong")
    }

    companion object {
        val ANONYMOUS_USER = User(nick = "Anonymous", token = TokenService.ANONYMOUS_TOKEN, role = UserRole.READ_ONLY)
    }
}