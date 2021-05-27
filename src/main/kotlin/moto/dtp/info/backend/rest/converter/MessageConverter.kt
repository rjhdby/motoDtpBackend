package moto.dtp.info.backend.rest.converter

import moto.dtp.info.backend.domain.message.Message
import moto.dtp.info.backend.exception.ImpossibleException
import moto.dtp.info.backend.rest.response.MessageResponse
import moto.dtp.info.backend.service.UserService
import moto.dtp.info.backend.service.UserService.Companion.UNKNOWN_USER_NICK
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class MessageConverter(
    private val userService: UserService
) {
    suspend fun toMessageResponse(message: Message) = MessageResponse(
        id = message.id?.toHexString() ?: throw ImpossibleException("Return message without ID attempted"),
        author = message.author.toHexString(),
        authorNick = userService.getUser(message.author)?.nick ?: UNKNOWN_USER_NICK,
        topic = message.topic.toHexString(),
        created = Date.from(Instant.ofEpochSecond(message.created)),
        hidden = message.hidden,
        text = message.text,
    )
}