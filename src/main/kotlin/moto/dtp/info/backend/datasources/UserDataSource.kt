package moto.dtp.info.backend.datasources

import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import moto.dtp.info.backend.domain.user.User
import moto.dtp.info.backend.repository.UserRepository
import moto.dtp.info.backend.security.JWTProvider
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class UserDataSource(
    private val userRepository: UserRepository,
    private val tokenService: JWTProvider
) {
    suspend fun getByToken(token: String): User {
        val id = tokenService.extractUserId(token)
        val persisted = userRepository.findById(ObjectId(id)).awaitFirstOrNull()

        return persisted ?: User.ANONYMOUS
    }

    suspend fun persist(user: User): User = userRepository.save(user).awaitFirst()

    suspend fun get(id: ObjectId): User? = userRepository.findById(id).awaitFirstOrNull()

    suspend fun invalidateAndGet(id: ObjectId): User? = userRepository.findById(id).awaitFirstOrNull()
}