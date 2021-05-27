package moto.dtp.info.backend.service

import moto.dtp.info.backend.configuration.MobileConfiguration
import moto.dtp.info.backend.datasources.AccidentDataSource
import moto.dtp.info.backend.datasources.UserDataSource
import moto.dtp.info.backend.domain.accident.Accident
import moto.dtp.info.backend.domain.accident.GeoConstraint
import moto.dtp.info.backend.domain.user.User
import moto.dtp.info.backend.exception.InsufficientRightsException
import moto.dtp.info.backend.exception.NotFoundException
import moto.dtp.info.backend.rest.request.CreateAccidentRequest
import moto.dtp.info.backend.service.filters.CanSeeAccidentFilter
import moto.dtp.info.backend.utils.TimeUtils
import org.springframework.stereotype.Service

@Service
class AccidentService(
    private val userDataSource: UserDataSource,
    private val notificatorService: NotificatorService,
    private val accidentDataSource: AccidentDataSource,
) {
    suspend fun getList(
        token: String,
        depth: Int,
        lastFetch: Long?,
        geoConstraint: GeoConstraint
    ): List<Accident> {
        val user = getUser(token)
        val from = TimeUtils.currentSec() - MobileConfiguration.adjustDepth(user.role, depth) * SECONDS_IN_HOUR

        return accidentDataSource.getListFrom(from)
            .filter { it.updated > lastFetch ?: from }
            .filter { CanSeeAccidentFilter.canSee(user, it) }
            .filter { geoConstraint.matches(it.location.getGeoPoint()) }
            .toList()
    }

    suspend fun get(token: String, id: String): Accident {
        val user = getUser(token)
        val from = TimeUtils.currentSec() - MobileConfiguration.adjustDepth(user.role, 356) * SECONDS_IN_HOUR

        return accidentDataSource.findOneFrom(id, from)?.takeIf { CanSeeAccidentFilter.canSee(user, it) }
               ?: throw NotFoundException()
    }

    suspend fun create(token: String, request: CreateAccidentRequest): Accident {
        val user = getUser(token)
        guardReadOnly(user)

        val currentMillis = TimeUtils.currentSec()

        val accident = Accident(
            created = currentMillis,
            updated = currentMillis,
            type = request.type,
            verified = false,
            hidden = false,
            hardness = request.hardness,
            creator = user.id!!,
            location = request.location,
            description = request.description
        )

        notificatorService.notifyCreated(accident)

        return accidentDataSource.persist(accident)
    }

    suspend fun update(token: String, id: String, request: CreateAccidentRequest): Accident {
        guardModerator(getUser(token))

        return applyChanges(id) {
            it.type = request.type
            it.hardness = request.hardness
            it.location = request.location
            it.description = request.description
        }
    }

    suspend fun setHidden(token: String, id: String, value: Boolean): Accident {
        guardModerator(getUser(token))

        return applyChanges(id) { it.hidden = value }
    }

    suspend fun setResolve(token: String, id: String, value: Boolean): Accident {
        guardReadOnly(getUser(token))

        return applyChanges(id) { it.resolved = if (value) TimeUtils.currentSec() else null }
    }

    suspend fun setConflict(token: String, id: String, value: Boolean): Accident {
        guardAdmin(getUser(token))
        val accident = applyChanges(id) { it.conflict = value }
        if (value) {
            notificatorService.notifyConflict(accident)
        } else {
            notificatorService.notifyConflictCanceled(accident)
        }

        return accident
    }

    private suspend fun applyChanges(id: String, mutator: (Accident) -> Unit): Accident {
        val accident = accidentDataSource.get(id) ?: throw NotFoundException()

        mutator(accident)
        accident.updated = TimeUtils.currentSec()

        return accidentDataSource.persist(accident)
    }

    private fun guardAdmin(user: User) {
        if (user.role.canManageConflict()) {
            return
        }

        throw InsufficientRightsException()
    }

    private fun guardModerator(user: User) {
        if (user.role.moderationAllowed()) {
            return
        }

        throw InsufficientRightsException()
    }

    private suspend fun getUser(token: String): User = userDataSource.getByToken(token)

    private fun guardReadOnly(user: User) {
        if (user.role.isReadonly()) {
            throw InsufficientRightsException()
        }
    }

    companion object {
        private const val SECONDS_IN_HOUR = 3600L
    }
}