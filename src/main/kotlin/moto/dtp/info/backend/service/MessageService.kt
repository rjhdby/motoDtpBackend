package moto.dtp.info.backend.service

import moto.dtp.info.backend.datasources.AccidentDataSource
import moto.dtp.info.backend.datasources.MessagesDataSource
import moto.dtp.info.backend.domain.message.Message
import moto.dtp.info.backend.domain.user.User
import moto.dtp.info.backend.exception.InsufficientRightsException
import moto.dtp.info.backend.exception.NotFoundException
import moto.dtp.info.backend.utils.TimeUtils
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class MessageService(
    private val messagesDataSource: MessagesDataSource,
    private val userService: UserService,
    private val accidentDataSource: AccidentDataSource,
) {
    suspend fun create(token: String, topic: String, text: String): Message {
        guardText(text)
        val user = userService.getUser(token) ?: throw InsufficientRightsException()
        if (user.role.isReadonly()) {
            throw InsufficientRightsException()
        }
        val accident = accidentDataSource.get(topic) ?: throw NotFoundException()
        val current = TimeUtils.currentSec()
        val message = Message(
            author = user.id!!,
            topic = accident.id!!,
            created = current,
            updated = current,
            text = text
        )

        return messagesDataSource.persist(message)
    }

    suspend fun getList(token: String, topic: String): List<Message> {
        accidentDataSource.get(topic)
        val user = userService.getUser(token) ?: UserService.ANONYMOUS_USER

        return messagesDataSource.getForTopic(topic) { canSee(it, user) }
    }

    suspend fun setHidden(token: String, id: String, value: Boolean): Message {
        return applyChanges(token, id) { it.hidden = value }
    }

    suspend fun modifyText(token: String, id: String, text: String): Message {
        guardText(text)
        return applyChanges(token, id) { it.text = text }
    }

    private suspend fun applyChanges(token: String, id: String, mutator: (Message) -> Unit): Message {
        val user = userService.getUser(token) ?: throw InsufficientRightsException()
        val message = messagesDataSource.get(ObjectId(id)) ?: throw NotFoundException()
        guardChangeInitiator(message, user)

        mutator(message)
        message.updated = TimeUtils.currentSec()

        return messagesDataSource.persist(message)
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