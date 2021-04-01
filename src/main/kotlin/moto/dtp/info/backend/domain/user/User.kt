package moto.dtp.info.backend.domain.user

import moto.dtp.info.backend.utils.TimeUtils
import org.bson.types.ObjectId

data class User(
    val id: ObjectId? = null,
    var token: String,
    val created: Long = TimeUtils.currentSec(),
    val nick: String,
    var role: UserRole = UserRole.USER
)
