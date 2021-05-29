package moto.dtp.info.backend.configuration

import moto.dtp.info.backend.domain.user.UserRole
import moto.dtp.info.backend.domain.user.UserRole.*
import java.lang.Integer.min

object MobileConfiguration {
    fun adjustDepthInHours(role: UserRole, requestedDepth: Int) = when (role) {
        DEVELOPER, SUPER_ADMIN -> requestedDepth
        ADMIN, MODERATOR       -> min(requestedDepth, 48)
        else                   -> min(requestedDepth, 24)
    }.toLong()
}