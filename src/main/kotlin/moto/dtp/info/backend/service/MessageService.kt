package moto.dtp.info.backend.service

import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import moto.dtp.info.backend.domain.message.Message
import moto.dtp.info.backend.domain.user.User
import moto.dtp.info.backend.exception.InsufficientRightsException
import moto.dtp.info.backend.exception.NotFoundException
import moto.dtp.info.backend.repository.MessageRepository
import moto.dtp.info.backend.utils.TimeUtils
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class MessageService(
    private val messageRepository: MessageRepository,
    private val userService: UserService,
    private val accidentService: AccidentService,
) {
    suspend fun create(token: String, topic: String, text: String): Message {
        guardText(text)
        val user = userService.getUser(token) ?: throw InsufficientRightsException()
        if (user.role.isReadonly()) {
            throw InsufficientRightsException()
        }
        val accident = accidentService.get(token, topic)
        val current = TimeUtils.currentSec()
        val message = Message(
            author = user.id!!,
            topic = accident.id!!,
            created = current,
            updated = current,
            text = text
        )

        return messageRepository.save(message).awaitFirst()
    }

    suspend fun getList(token: String, topic: String): List<Message> {
        accidentService.get(token, topic)
        val user = userService.getUser(token) ?: UserService.ANONYMOUS_USER

        return messageRepository.findAllByTopic(ObjectId(topic))
            .asFlow()
            .filter { canSee(it, user) }
            .toList()
    }

    suspend fun setHidden(token: String, id: String, value: Boolean): Message {
        val user = userService.getUser(token) ?: throw InsufficientRightsException()
        val message = messageRepository.findById(ObjectId(id)).awaitFirstOrNull() ?: throw NotFoundException()
        guardChangeInitiator(message, user)

        message.hidden = value
        message.updated = TimeUtils.currentSec()

        return messageRepository.save(message).awaitFirst()
    }

    suspend fun modifyText(token: String, id: String, text: String): Message {
        guardText(text)
        val user = userService.getUser(token) ?: throw InsufficientRightsException()
        val message = messageRepository.findById(ObjectId(id)).awaitFirstOrNull() ?: throw NotFoundException()
        guardChangeInitiator(message, user)

        message.text = text
        message.updated = TimeUtils.currentSec()

        return messageRepository.save(message).awaitFirst()
    }

    private fun canSee(message: Message, user: User): Boolean = when {
        !message.hidden               -> true
        user.role.moderationAllowed() -> true
        message.author == user.id     -> true
        else                          -> false
    }

    private fun guardText(text: String) {
        if (text.length > 250) {
            throw IllegalArgumentException("Message text must be less that 250 symbols")
        }
    }

    private fun guardChangeInitiator(message: Message, user: User) {
        if (!user.role.moderationAllowed() && message.author != user.id) {
            throw InsufficientRightsException()
        }
    }
}