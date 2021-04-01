package moto.dtp.info.backend.repository

import moto.dtp.info.backend.domain.user.User
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface UserRepository : ReactiveMongoRepository<User, ObjectId> {
    @Query(value = "{'token' :?0}")
    fun findByToken(token: String): Mono<User>
}