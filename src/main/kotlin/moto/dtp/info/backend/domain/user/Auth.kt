package moto.dtp.info.backend.domain.user

import org.bson.types.ObjectId

data class Auth(
    val id: ObjectId? = null,
    val uid: ObjectId,
    val type: AuthType,
    val credentials: Credentials? = null,
    val vk: VkUser? = null
)
