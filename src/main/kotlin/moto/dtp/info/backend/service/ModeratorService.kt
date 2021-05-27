package moto.dtp.info.backend.service

import moto.dtp.info.backend.domain.user.User
import moto.dtp.info.backend.domain.user.UserRole
import moto.dtp.info.backend.domain.user.UserRole.*
import moto.dtp.info.backend.exception.InsufficientRightsException
import moto.dtp.info.backend.exception.NotFoundException
import moto.dtp.info.backend.rest.response.UserResponse
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class ModeratorService(
    private val userService: UserService,
) {
    suspend fun setRole(token: String, uid: String, role: UserRole): UserResponse {
        val admin = userService.getUserByToken(token)
        guardModerator(admin)
        val user = userService.getUser(ObjectId(uid)) ?: throw NotFoundException()
        val roles = setOf(role, user.role)
        when {
            role.manualChangeOnly() || user.role.manualChangeOnly() -> throw InsufficientRightsException()
            ADMIN in roles && !admin.role.canManageAdmins()         -> throw InsufficientRightsException()
            MODERATOR in roles && !admin.role.canManageModerators() -> throw InsufficientRightsException()
            READ_ONLY in roles && !admin.role.moderationAllowed()   -> throw InsufficientRightsException()
        }

        user.role = role

        return UserResponse.fromUser(userService.persist(user))
    }

    private fun guardModerator(user: User) {
        if (user.role.moderationAllowed()) {
            return
        }

        throw InsufficientRightsException()
    }
}