package moto.dtp.info.backend.domain.accident

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import moto.dtp.info.backend.rest.response.AccidentResponse
import org.bson.types.ObjectId
import java.time.Instant
import java.util.*

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
) {
    fun toAccidentResponse(): AccidentResponse = AccidentResponse(
        id = id?.toHexString(),
        created = Date.from(Instant.ofEpochSecond(created)),
        type = type,
        resolved = resolved?.let { Date.from(Instant.ofEpochSecond(it)) },
        verified = verified,
        hidden = hidden,
        hardness = hardness,
        creator = creator.toHexString(),
        location = location,
        description = description,
        conflict = conflict
    )
}
