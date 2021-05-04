package moto.dtp.info.backend.service.filters

import moto.dtp.info.backend.domain.user.User

interface CanSeeFilter<ENTRY> {
    fun canSee(user: User, entry: ENTRY): Boolean
}