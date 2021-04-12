package moto.dtp.info.backend.datasources

import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import moto.dtp.info.backend.domain.message.Message
import moto.dtp.info.backend.domain.message.TopicMessages
import moto.dtp.info.backend.repository.MessageRepository
import moto.dtp.info.backend.utils.TimeUtils.DAY
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class MessagesDataSource(
    private val messageRepository: MessageRepository,
    private val accidentDataSource: AccidentDataSource,
    private val userDataSource: UserDataSource,
) {
    private val cache: Cache<String, TopicMessages> = Cache(500) { entry: TopicMessages, currentTime: Long ->
        entry.created < currentTime - DAY * 2
    }

    suspend fun getForTopic(topic: String, filter: suspend (Message) -> Boolean): List<Message> {
        val cached = cache.get(topic)
        if (cached != null) {
            return cached.getMessages()
        }

        val persisted = messageRepository.findAllByTopic(ObjectId(topic))
            .asFlow()
            .filter(filter)
            .toList()

        val accident = accidentDataSource.get(topic) ?: return emptyList()
        val topicMessages = TopicMessages(accident.id!!, accident.created)
        topicMessages.putAll(persisted)
        cache.put(accident.id.toHexString(), topicMessages)

        return persisted
    }

    suspend fun persist(message: Message): Message {
        cache.get(message.topic.toHexString())?.put(message)

        return messageRepository.save(message).awaitFirst()
    }

    suspend fun get(id: ObjectId): Message? = cache.entries()
        .firstOrNull { it.value.get(id) != null }
        ?.value?.get(id)
        ?: messageRepository.findById(id).awaitFirstOrNull()
}