package moto.dtp.info.backend.datasources

import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import moto.dtp.info.backend.domain.message.Message
import moto.dtp.info.backend.repository.MessageRepository
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class MessagesDataSource(
    private val messageRepository: MessageRepository,
) {
    suspend fun getForTopic(topic: String, filter: suspend (Message) -> Boolean): List<Message> {
        return messageRepository.findAllByTopic(ObjectId(topic))
            .asFlow()
            .filter(filter)
            .toList()
    }

    suspend fun persist(message: Message): Message {
        return messageRepository.save(message).awaitFirst()
    }

    suspend fun get(id: ObjectId): Message? = messageRepository.findById(id).awaitFirstOrNull()
}