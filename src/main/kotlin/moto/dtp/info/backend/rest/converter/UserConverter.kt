package moto.dtp.info.backend.rest.converter

import moto.dtp.info.backend.domain.user.User
import moto.dtp.info.backend.rest.response.UserResponse
import org.springframework.stereotype.Component

@Component
class UserConverter {
    suspend fun toUserResponse(user: User) = UserResponse(
        id = user.id?.toHexString(),
        nick = user.nick,
        role = user.role
    )
}