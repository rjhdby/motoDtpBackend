package moto.dtp.info.backend.datasources

import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import moto.dtp.info.backend.domain.user.User
import moto.dtp.info.backend.repository.UserRepository
import moto.dtp.info.backend.security.JWTProvider
import moto.dtp.info.backend.utils.TimeUtils
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class UserDataSource(
    private val userRepository: UserRepository,
    private val tokenService: JWTProvider
) {
    private val cache: Cache<String, Pair<User, Long>> = Cache(500) { entry: Pair<User, Long>, currentTime: Long ->
        entry.second < currentTime - TimeUtils.HOUR * 4
    }

    suspend fun getByToken(token: String): User {
        val id = tokenService.extractUserId(token)
        val cached = cache.get(id)
        if (cached != null) {
            cache.put(id, Pair(cached.first, System.currentTimeMillis()))

            return cached.first
        }

        val persisted = userRepository.findById(ObjectId(id)).awaitFirstOrNull()
        if (persisted != null) {
            cache.put(persisted.id!!.toHexString(), Pair(persisted, System.currentTimeMillis()))
        }

        return persisted ?: User.ANONYMOUS
    }

    suspend fun persist(user: User): User {
        val persisted = userRepository.save(user).awaitFirst()
        cache.put(persisted.id!!.toHexString(), Pair(persisted, System.currentTimeMillis()))

        return persisted
    }

    suspend fun get(uid: String): User? {
        val cached = cache.get(uid)?.first
        if (cached != null) {
            cache.put(cached.id!!.toHexString(), Pair(cached, System.currentTimeMillis()))
            return cached
        }

        val persisted = userRepository.findById(ObjectId(uid)).awaitFirstOrNull()
        if (persisted != null) {
            cache.put(persisted.id!!.toHexString(), Pair(persisted, System.currentTimeMillis()))
        }

        return persisted
    }
}