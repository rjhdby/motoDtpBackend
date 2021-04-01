package moto.dtp.info.backend.domain.accident

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import org.bson.types.ObjectId

data class Accident(
    @field:JsonSerialize(using = ToStringSerializer::class) val id: ObjectId? = null,
    val created: Long,
    var updated: Long,
    var type: AccidentType,
    var resolved: Long? = null,
    var verified: Boolean,
    var hidden: Boolean,
    var hardness: AccidentHardness,
    @field:JsonSerialize(using = ToStringSerializer::class) val creator: ObjectId,
    var location: Address,
    var description: String,
    var conflict: Boolean = false
)
