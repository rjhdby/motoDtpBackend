package moto.dtp.info.backend.repository

import moto.dtp.info.backend.domain.accident.Accident
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface AccidentRepository : ReactiveMongoRepository<Accident, ObjectId> {
    @Query(value = "{created: {\$gte: ?0}, hidden: ?1}")
    suspend fun findFrom(from: Long, hidden: Boolean): Flux<Accident>

    @Query(value = "{_id: ?0, created: {\$gte: ?1}, hidden: ?2}")
    suspend fun findOneFrom(id: ObjectId, from: Long, hidden: Boolean): Mono<Accident>
}