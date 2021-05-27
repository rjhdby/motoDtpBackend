package moto.dtp.info.backend.domain.user

import java.util.*

enum class UserRole {
    READ_ONLY, USER, MODERATOR, ADMIN, DEVELOPER, SUPER_ADMIN;

    fun manualChangeOnly() = this in EnumSet.of(DEVELOPER, SUPER_ADMIN)
    fun canManageModerators() = this in EnumSet.of(ADMIN, DEVELOPER, SUPER_ADMIN)
    fun canManageConflict() = canManageModerators()
    fun canManageAdmins() = this in EnumSet.of(DEVELOPER, SUPER_ADMIN)
    fun moderationAllowed() = this in EnumSet.of(MODERATOR, ADMIN, DEVELOPER, SUPER_ADMIN)
    fun isReadonly() = this == READ_ONLY
}