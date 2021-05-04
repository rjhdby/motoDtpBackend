package moto.dtp.info.backend.security

import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import moto.dtp.info.backend.domain.user.Auth
import moto.dtp.info.backend.domain.user.AuthType
import moto.dtp.info.backend.domain.user.Credentials
import moto.dtp.info.backend.domain.user.User
import moto.dtp.info.backend.repository.AuthRepository
import moto.dtp.info.backend.repository.UserRepository
import moto.dtp.info.backend.rest.request.AuthRequest
import moto.dtp.info.backend.utils.ThrowUtils.throwInternal
import org.springframework.stereotype.Service

@Service
class BasicAuthorization(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    suspend fun authorize(authRequest: AuthRequest.Basic): User {
        val auth = authRepository.findByLogin(authRequest.login).awaitFirstOrNull()
        if (auth != null) {
            auth.credentials?.guardPassword(authRequest.password) ?: throwInternal()
            return userRepository.findById(auth.uid).awaitFirstOrNull() ?: throwInternal()
        }

        val user = userRepository.save(User(nick = authRequest.nick ?: authRequest.login)).awaitFirst()
        authRepository.save(
            Auth(
                uid = user.id!!,
                type = AuthType.BASIC,
                credentials = Credentials.fromRequest(authRequest)
            )
        ).awaitFirst()

        return user
    }
}