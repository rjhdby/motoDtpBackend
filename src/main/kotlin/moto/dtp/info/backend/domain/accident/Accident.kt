package moto.dtp.info.backend.domain.accident

import moto.dtp.info.backend.domain.EntityWithId
import org.bson.types.ObjectId

data class Accident(
    override val id: ObjectId? = null,
    var type: AccidentType,
    val created: Long,
    val creator: ObjectId,
    var location: Address,
    var updated: Long = created,
    var resolved: Long? = null,
    var verified: Boolean = false,
    var hidden: Boolean = false,
    var hardness: AccidentHardness? = null,
    var description: String = "",
    var conflict: Boolean = false
) : EntityWithId
