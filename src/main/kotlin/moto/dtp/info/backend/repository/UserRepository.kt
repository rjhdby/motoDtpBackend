package moto.dtp.info.backend.repository

import moto.dtp.info.backend.domain.user.User
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : ReactiveMongoRepository<User, ObjectId>