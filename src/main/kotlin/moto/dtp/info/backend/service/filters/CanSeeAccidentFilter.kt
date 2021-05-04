package moto.dtp.info.backend.service.filters

import moto.dtp.info.backend.domain.accident.Accident
import moto.dtp.info.backend.domain.user.User

object CanSeeAccidentFilter : CanSeeFilter<Accident> {
    override fun canSee(user: User, entry: Accident): Boolean = when {
        user.role.moderationAllowed() -> true
        else                          -> !entry.hidden
    }
}