package moto.dtp.info.backend.domain.message

import org.bson.types.ObjectId
import java.util.concurrent.ConcurrentHashMap

class TopicMessages(val topic: ObjectId, val created: Long) {
    private val messages: ConcurrentHashMap<ObjectId, Message> = ConcurrentHashMap(1, 0.75f, 2)

    fun put(message: Message) {
        messages[message.id ?: return] = message
    }

    fun putAll(messages: Iterable<Message>) {
        messages.forEach {
            this.messages[it.id!!] = it
        }
    }

    fun getMessages() = messages.values.toList()

    fun get(id: ObjectId) = messages[id]
}