package moto.dtp.info.backend.service

import moto.dtp.info.backend.datasources.AccidentDataSource
import moto.dtp.info.backend.datasources.MessagesDataSource
import moto.dtp.info.backend.domain.accident.Accident
import moto.dtp.info.backend.domain.message.Message
import moto.dtp.info.backend.domain.user.User
import moto.dtp.info.backend.exception.InsufficientRightsException
import moto.dtp.info.backend.exception.NotFoundException
import moto.dtp.info.backend.service.filters.CanSeeAccidentFilter
import moto.dtp.info.backend.service.filters.CanSeeMessageFilter
import moto.dtp.info.backend.utils.TimeUtils
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@ExperimentalContracts
@Service
class MessageService(
    private val messagesDataSource: MessagesDataSource,
    private val userService: UserService,
    private val accidentDataSource: AccidentDataSource,
) {
    suspend fun create(token: String, topic: String, text: String): Message {
        guardText(text)
        val user = userService.getUser(token)
        if (user.role.isReadonly()) {
            throw InsufficientRightsException()
        }
        val accident = accidentDataSource.get(topic)
        guardCanSeeAccident(user, accident)

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
        val user = userService.getUser(token)
        val accident = accidentDataSource.get(topic)
        guardCanSeeAccident(user, accident)

        return messagesDataSource.getForTopic(topic) { CanSeeMessageFilter.canSee(user, it) }
    }

    suspend fun setHidden(token: String, id: String): Message {
        return applyChanges(token, id) { it.hidden = true }
    }

    suspend fun resetHidden(token: String, id: String): Message {
        return applyChanges(token, id) { it.hidden = false }
    }

    suspend fun modifyText(token: String, id: String, text: String): Message {
        guardText(text)
        return applyChanges(token, id) { it.text = text }
    }

    private suspend fun applyChanges(token: String, id: String, mutator: (Message) -> Unit): Message {
        val user = userService.getUser(token)
        val message = messagesDataSource.get(ObjectId(id)) ?: throw NotFoundException()
        guardChangeInitiator(message, user)

        mutator(message)
        message.updated = TimeUtils.currentSec()

        return messagesDataSource.persist(message)
    }

    private suspend fun guardCanSeeAccident(user: User, accident: Accident?) {
        contract { returns() implies (accident != null) }
        if (accident == null || !CanSeeAccidentFilter.canSee(user, accident)) {
            throw NotFoundException()
        }
    }

    private fun guardText(text: String) {
        if (text.length > MAX_MESSAGE_LENGTH) {
            throw IllegalArgumentException("Message text must be less that $MAX_MESSAGE_LENGTH symbols")
        }
    }

    private fun guardChangeInitiator(message: Message, user: User) {
        if (!user.role.moderationAllowed() && message.author != user.id) {
            throw InsufficientRightsException()
        }
    }

    companion object {
        private const val MAX_MESSAGE_LENGTH = 500
    }
}