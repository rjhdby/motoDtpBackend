package moto.dtp.info.backend.domain.message

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import moto.dtp.info.backend.domain.EntityWithId
import org.bson.types.ObjectId

data class Message(
    @field:JsonSerialize(using = ToStringSerializer::class) override val id: ObjectId? = null,
    @field:JsonSerialize(using = ToStringSerializer::class) val author: ObjectId,
    @field:JsonSerialize(using = ToStringSerializer::class) val topic: ObjectId,
    val created: Long,
    var updated: Long,
    var hidden: Boolean = false,
    var text: String
) : EntityWithId