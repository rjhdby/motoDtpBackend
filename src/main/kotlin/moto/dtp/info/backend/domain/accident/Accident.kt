package moto.dtp.info.backend.domain.accident

import moto.dtp.info.backend.domain.EntityWithId
import org.bson.types.ObjectId

data class Accident(
    override val id: ObjectId? = null,
    val created: Long,
    var updated: Long,
    var type: AccidentType,
    var resolved: Long? = null,
    var verified: Boolean,
    var hidden: Boolean,
    var hardness: AccidentHardness? = null,
    val creator: ObjectId,
    var location: Address,
    var description: String,
    var conflict: Boolean = false
) : EntityWithId
