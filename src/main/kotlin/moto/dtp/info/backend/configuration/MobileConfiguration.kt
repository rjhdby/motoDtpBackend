package moto.dtp.info.backend.configuration

import moto.dtp.info.backend.domain.user.UserRole
import java.lang.Integer.max

object MobileConfiguration {
    fun adjustDepth(role: UserRole, requestedDepth: Int) = when (role) {
        UserRole.DEVELOPER                 -> requestedDepth
        UserRole.ADMIN, UserRole.MODERATOR -> max(requestedDepth, 48)
        else                               -> max(requestedDepth, 24)
    }.toLong()
}