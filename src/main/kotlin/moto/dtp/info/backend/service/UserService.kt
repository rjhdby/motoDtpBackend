package moto.dtp.info.backend.service

import moto.dtp.info.backend.datasources.UserDataSource
import moto.dtp.info.backend.domain.user.User
import moto.dtp.info.backend.exception.NotFoundException
import moto.dtp.info.backend.rest.request.AuthRequest
import moto.dtp.info.backend.rest.response.UserResponse
import moto.dtp.info.backend.security.BasicAuthorization
import moto.dtp.info.backend.security.JWTProvider
import moto.dtp.info.backend.security.VkAuthorization
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class UserService(
    private val authRequestValidator: AuthRequestValidator,
    private val tokenService: JWTProvider,
    private val userDataSource: UserDataSource,
    private val vkAuthorization: VkAuthorization,
    private val basicAuthorization: BasicAuthorization,
) {
    suspend fun getUserByToken(token: String): User = userDataSource.getByToken(token)

    suspend fun getUser(id: ObjectId): User? = userDataSource.get(id)

    suspend fun register(request: AuthRequest): String {
        authRequestValidator.validate(request)

        val user = when (request) {
            AuthRequest.Anonymous -> User.ANONYMOUS
            is AuthRequest.Basic  -> basicAuthorization.authorize(request)
            is AuthRequest.VK     -> vkAuthorization.authorize(request)
        }

        return tokenService.createToken(user)
    }

    suspend fun persist(user: User): User {
        return userDataSource.persist(user)
    }

    suspend fun invalidate(id: String): User {
        return userDataSource.invalidateAndGet(ObjectId(id)) ?: throw NotFoundException()
    }

    companion object {
        const val UNKNOWN_USER_NICK = "Хер с горы"
    }
}