package moto.dtp.info.backend.domain.message

import org.bson.types.ObjectId

data class Message(
    val id: ObjectId? = null,
    val author: ObjectId,
    val topic: ObjectId,
    val created: Long,
    var updated: Long,
    var hidden: Boolean = false,
    var text: String
)