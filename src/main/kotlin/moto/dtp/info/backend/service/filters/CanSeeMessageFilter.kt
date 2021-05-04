package moto.dtp.info.backend.service.filters

import moto.dtp.info.backend.domain.message.Message
import moto.dtp.info.backend.domain.user.User

object CanSeeMessageFilter : CanSeeFilter<Message> {
    override fun canSee(user: User, entry: Message): Boolean = when {
        !entry.hidden                 -> true
        user.role.moderationAllowed() -> true
        entry.author == user.id       -> true
        else                          -> false
    }
}