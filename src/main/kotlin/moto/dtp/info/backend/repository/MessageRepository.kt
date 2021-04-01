package moto.dtp.info.backend.repository

import moto.dtp.info.backend.domain.message.Message
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface MessageRepository : ReactiveMongoRepository<Message, ObjectId> {
    @Query(value = "{'topic' :?0}")
    suspend fun findAllByTopic(topic: ObjectId): Flux<Message>
}