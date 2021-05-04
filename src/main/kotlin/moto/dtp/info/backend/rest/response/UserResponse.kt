package moto.dtp.info.backend.rest.response

import moto.dtp.info.backend.domain.user.User
import moto.dtp.info.backend.domain.user.UserRole

data class UserResponse(val id: String?, val nick: String, val role: UserRole) {
    companion object {
        fun fromUser(user: User) = UserResponse(user.id?.toHexString() ?: "anonymous", user.nick, user.role)
    }
}
