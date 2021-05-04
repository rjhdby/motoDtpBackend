package moto.dtp.info.backend.service

import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import moto.dtp.info.backend.datasources.UserDataSource
import moto.dtp.info.backend.domain.user.Auth
import moto.dtp.info.backend.domain.user.AuthType
import moto.dtp.info.backend.domain.user.Credentials
import moto.dtp.info.backend.domain.user.User
import moto.dtp.info.backend.repository.AuthRepository
import moto.dtp.info.backend.rest.request.AuthRequest
import moto.dtp.info.backend.rest.response.UserResponse
import moto.dtp.info.backend.security.JWTProvider
import org.springframework.stereotype.Service

@Service
class UserService(
    private val authRepository: AuthRepository,
    private val authRequestValidator: AuthRequestValidator,
    private val tokenService: JWTProvider,
    private val userDataSource: UserDataSource,
) {
    suspend fun getUser(token: String): User = userDataSource.getByToken(token)

    suspend fun getUserResponse(token: String): UserResponse = UserResponse.fromUser(getUser(token))

    suspend fun register(request: AuthRequest): String {
        authRequestValidator.validate(request)
        tryToLogin(request)?.let { return it }

        return when (request) {
            AuthRequest.Anonymous -> tokenService.createToken(User.ANONYMOUS)
            is AuthRequest.Basic  -> registerBasic(request)
        }
    }

    private suspend fun tryToLogin(request: AuthRequest): String? {
        val auth = when (request) {
            AuthRequest.Anonymous -> return tokenService.createToken(User.ANONYMOUS)
            is AuthRequest.Basic  -> authRepository.findByLogin(request.login).awaitFirstOrNull() ?: return null
        }

        return login(request, auth)
    }

    private suspend fun registerBasic(request: AuthRequest.Basic): String {
        val user = userDataSource.persist(User(nick = request.nick ?: "Unknown"))
        val auth = Auth(
            uid = user.id!!,
            type = AuthType.BASIC,
            credentials = Credentials.fromRequest(request),
        )

        authRepository.save(auth).awaitFirst()

        return tokenService.createToken(user)
    }

    private suspend fun login(request: AuthRequest, auth: Auth): String {
        when (request) {
            is AuthRequest.Basic  -> auth.credentials?.guardPassword(request.password) ?: throwInternal()
            AuthRequest.Anonymous -> Unit
        }

        val user = userDataSource.get(auth.uid.toHexString()) ?: throwInternal()

        return tokenService.createToken(user)
    }

    private fun throwInternal(): Nothing {
        throw InternalError("Something went terrible wrong")
    }
}