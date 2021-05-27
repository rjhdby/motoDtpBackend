package moto.dtp.info.backend.rest.converter

import moto.dtp.info.backend.datasources.MessagesDataSource
import moto.dtp.info.backend.domain.accident.Accident
import moto.dtp.info.backend.domain.user.User
import moto.dtp.info.backend.exception.ImpossibleException
import moto.dtp.info.backend.rest.response.AccidentResponse
import moto.dtp.info.backend.service.UserService
import moto.dtp.info.backend.service.filters.CanSeeMessageFilter
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class AccidentConverter(
    private val userService: UserService,
    private val messagesDataSource: MessagesDataSource
) {
    suspend fun toAccidentResponse(accident: Accident) = AccidentResponse(
        id = accident.id?.toHexString() ?: throw ImpossibleException("Return accident without ID attempted"),
        created = Date.from(Instant.ofEpochSecond(accident.created)),
        type = accident.type,
        resolved = accident.resolved?.let { Date.from(Instant.ofEpochSecond(it)) },
        verified = accident.verified,
        hidden = accident.hidden,
        hardness = accident.hardness,
        creator = accident.creator.toHexString(),
        creatorNick = userService.getUser(accident.creator)?.nick ?: UserService.UNKNOWN_USER_NICK,
        location = accident.location,
        description = accident.description,
        conflict = accident.conflict,
        messages = countMessages(accident.creator, accident.id)
    )

    private suspend fun countMessages(userId: ObjectId, topicId: ObjectId): Int {
        val user = userService.getUser(userId) ?: User.ANONYMOUS

        return messagesDataSource.getForTopic(topicId.toHexString()) {
            CanSeeMessageFilter.canSee(user, it)
        }.count()
    }
}