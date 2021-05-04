package moto.dtp.info.backend.service

import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import moto.dtp.info.backend.domain.user.User
import moto.dtp.info.backend.domain.user.UserRole
import moto.dtp.info.backend.domain.user.UserRole.*
import moto.dtp.info.backend.exception.InsufficientRightsException
import moto.dtp.info.backend.exception.NotFoundException
import moto.dtp.info.backend.repository.UserRepository
import moto.dtp.info.backend.rest.response.UserResponse
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class ModeratorService(
    private val userService: UserService,
    private val userRepository: UserRepository,
) {
    suspend fun setRole(token: String, uid: String, role: UserRole): UserResponse {
        val admin = userService.getUser(token)
        guardAdmin(admin)
        val user = userRepository.findById(ObjectId(uid)).awaitFirstOrNull() ?: throw NotFoundException()
        val roles = setOf(role, user.role)
        when {
            role.manualChangeOnly() || user.role.manualChangeOnly() -> throw InsufficientRightsException()
            ADMIN in roles && !admin.role.canManageAdmins()         -> throw InsufficientRightsException()
            MODERATOR in roles && !admin.role.canManageModerators() -> throw InsufficientRightsException()
            READ_ONLY in roles && !admin.role.moderationAllowed()   -> throw InsufficientRightsException()
        }

        user.role = role

        userRepository.save(user).awaitFirst()

        return UserResponse.fromUser(user)
    }

    private fun guardAdmin(user: User) {
        if (user.role !in listOf(ADMIN, DEVELOPER, SUPER_ADMIN)) {
            throw InsufficientRightsException()
        }
    }
}