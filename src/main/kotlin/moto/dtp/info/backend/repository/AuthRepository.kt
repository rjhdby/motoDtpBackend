package moto.dtp.info.backend.repository

import moto.dtp.info.backend.domain.user.Auth
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface AuthRepository : ReactiveMongoRepository<Auth, ObjectId> {
    @Query(value = "{'credentials.login' :?0}")
    suspend fun findByLogin(login: String): Mono<Auth>

    @Query(value = "{'vk.id' :?0}")
    suspend fun findByVkId(vkId: Int): Mono<Auth>
}