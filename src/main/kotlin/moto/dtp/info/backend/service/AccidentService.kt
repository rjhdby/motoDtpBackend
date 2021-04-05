package moto.dtp.info.backend.service

import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import moto.dtp.info.backend.configuration.MobileConfiguration
import moto.dtp.info.backend.domain.accident.Accident
import moto.dtp.info.backend.domain.accident.GeoConstraint
import moto.dtp.info.backend.domain.user.User
import moto.dtp.info.backend.domain.user.UserRole
import moto.dtp.info.backend.exception.InsufficientRightsException
import moto.dtp.info.backend.exception.NotFoundException
import moto.dtp.info.backend.repository.AccidentRepository
import moto.dtp.info.backend.rest.request.CreateAccidentRequest
import moto.dtp.info.backend.utils.TimeUtils
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class AccidentService(
    private val userService: UserService,
    private val accidentRepository: AccidentRepository,
    private val notificatorService: NotificatorService,
) {
    suspend fun getList(token: String, depth: Int, lastFetch: Long?, geoConstraint: GeoConstraint): List<Accident> {
        val user = userService.getUser(token) ?: UserService.ANONYMOUS_USER
        val from = TimeUtils.currentSec() - MobileConfiguration.adjustDepth(user.role, depth) * SECONDS_IN_HOUR

        return accidentRepository.findFrom(from)
            .asFlow()
            .filter { it.updated > lastFetch ?: from }
            .filter { canBeShowedToRole(user.role, it) }
            .filter { geoConstraint.matches(it.location.getGeoPoint()) }
            .toList()
    }

    suspend fun get(token: String, id: String): Accident {
        val user = userService.getUser(token) ?: UserService.ANONYMOUS_USER
        val from = TimeUtils.currentSec() - MobileConfiguration.adjustDepth(user.role, 356) * SECONDS_IN_HOUR

        return accidentRepository.findOneFrom(ObjectId(id), from).awaitFirstOrNull()
            ?.takeIf { canBeShowedToRole(user.role, it) }
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

        return accidentRepository.insert(accident).awaitFirst()
    }

    suspend fun update(token: String, id: String, request: CreateAccidentRequest): Accident {
        guardReadOnly(getUser(token))

        val accident = accidentRepository.findById(ObjectId(id)).awaitFirstOrNull() ?: throw NotFoundException()
        accident.updated = TimeUtils.currentSec()
        accident.type = request.type
        accident.hardness = request.hardness
        accident.location = request.location
        accident.description = request.description

        return accidentRepository.save(accident).awaitFirst()
    }

    suspend fun setHidden(token: String, id: String, value: Boolean): Accident {
        guardReadOnly(getUser(token))

        val accident = accidentRepository.findById(ObjectId(id)).awaitFirstOrNull() ?: throw NotFoundException()
        accident.hidden = value
        accident.updated = TimeUtils.currentSec()

        return accidentRepository.save(accident).awaitFirst()
    }

    suspend fun setResolve(token: String, id: String, value: Boolean): Accident {
        guardReadOnly(getUser(token))

        val accident = accidentRepository.findById(ObjectId(id)).awaitFirstOrNull() ?: throw NotFoundException()
        accident.resolved = if (value) TimeUtils.currentSec() else null
        accident.updated = TimeUtils.currentSec()

        return accidentRepository.save(accident).awaitFirst()
    }

    suspend fun setConflict(token: String, id: String, value: Boolean): Accident {
        guardAdmin(getUser(token))

        val accident = accidentRepository.findById(ObjectId(id)).awaitFirstOrNull() ?: throw NotFoundException()
        accident.conflict = value
        accident.updated = TimeUtils.currentSec()

        if (value) {
            notificatorService.notifyConflict(accident)
        } else {
            notificatorService.notifyConflictCanceled(accident)
        }

        return accidentRepository.save(accident).awaitFirst()
    }

    private fun canBeShowedToRole(role: UserRole, accident: Accident) = when {
        role.moderationAllowed() -> true
        else                     -> !accident.hidden
    }

    private fun guardAdmin(user: User) {
        when (user.role) {
            UserRole.ADMIN, UserRole.DEVELOPER -> return
            else                               -> throw InsufficientRightsException()
        }
    }

    private suspend fun getUser(token: String): User = userService.getUser(token) ?: UserService.ANONYMOUS_USER

    private fun guardReadOnly(user: User) {
        if (user.role.isReadonly()) {
            throw InsufficientRightsException()
        }
    }

    companion object {
        private const val SECONDS_IN_HOUR = 3600L
    }
}