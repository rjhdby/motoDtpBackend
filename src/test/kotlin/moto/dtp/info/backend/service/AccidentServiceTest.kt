package moto.dtp.info.backend.service

import assertThrowsSuspend
import kotlinx.coroutines.runBlocking
import mock.AccidentDataSourceMock
import mock.AccidentDataSourceMock.accident
import mock.AccidentDataSourceMock.moscow
import mock.AccidentDataSourceMock.voronezh
import mock.UserDataSourceMock
import moto.dtp.info.backend.domain.accident.Accident
import moto.dtp.info.backend.domain.accident.AccidentType
import moto.dtp.info.backend.domain.accident.Address
import moto.dtp.info.backend.domain.accident.GeoConstraint
import moto.dtp.info.backend.domain.user.UserRole
import moto.dtp.info.backend.domain.user.UserRole.*
import moto.dtp.info.backend.exception.InsufficientRightsException
import moto.dtp.info.backend.rest.request.CreateAccidentRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.Duration
import java.util.stream.Stream

internal class AccidentServiceTest {
    private var userDataSource = UserDataSourceMock.getInstance()

    @BeforeEach
    fun setUp() {
        userDataSource = UserDataSourceMock.getInstance()
    }

    @Test
    fun `it returns accidents`() = runBlocking {
        val accidentService = getService { generateSequence { accident() }.take(5).toList() }
        val result = accidentService.getList(USER.name, 10, null, GeoConstraint.fromAddress(moscow))
        assertEquals(5, result.size)
    }

    @ParameterizedTest
    @MethodSource("hiddenDataset")
    fun `it correctly returns hidden accidents`(role: UserRole, expected: Int) = runBlocking {
        val user = userDataSource.getByToken(USER.name)
        val accidentService = getService {
            generateSequence { accident() }.take(5).toMutableList().apply {
                add(accident(creator = user.id!!, hidden = true))
                add(accident(hidden = true))
            }
        }
        val result = accidentService.getList(role.name, 10, null, GeoConstraint.fromAddress(moscow))
        assertEquals(expected, result.size)
    }

    @ParameterizedTest
    @MethodSource("depthDataset")
    fun `it correctly returns depends of depth`(role: UserRole, expected: Int) = runBlocking {
        val accidentService = getService { depthTestAccidents }
        val result = accidentService.getList(role.name, 200, null, GeoConstraint.fromAddress(moscow))
        assertEquals(expected, result.size)
    }

    @ParameterizedTest
    @MethodSource("lastFetchDataset")
    fun `it correctly returns depends of lastFetch`(role: UserRole, expected: Int) = runBlocking {
        val accidentService = getService { depthTestAccidents }
        val result = accidentService.getList(role.name, 200, hoursAgo15, GeoConstraint.fromAddress(moscow))
        assertEquals(expected, result.size)
    }

    @Test
    fun `it restrict modification depends of role`() = runBlocking {
        val accident = accident()
        val createRequest = CreateAccidentRequest(
            type = AccidentType.MOTO_AUTO,
            hardness = null,
            location = Address(lat = 0.0f, lon = 0.0f, address = ""),
            description = ""
        )
        val id = accident.id!!.toHexString()
        val accidentService = getService { listOf(accident) }

        assertThrowsSuspend<InsufficientRightsException> { accidentService.create(READ_ONLY.name, createRequest) }
        accidentService.create(USER.name, createRequest)
        accidentService.create(MODERATOR.name, createRequest)
        accidentService.create(ADMIN.name, createRequest)
        accidentService.create(SUPER_ADMIN.name, createRequest)
        accidentService.create(DEVELOPER.name, createRequest)

        assertThrowsSuspend<InsufficientRightsException> { accidentService.setResolve(READ_ONLY.name, id, true) }
        accidentService.setResolve(USER.name, id, true)
        accidentService.setResolve(MODERATOR.name, id, true)
        accidentService.setResolve(ADMIN.name, id, true)
        accidentService.setResolve(SUPER_ADMIN.name, id, true)
        accidentService.setResolve(DEVELOPER.name, id, true)

        assertThrowsSuspend<InsufficientRightsException> { accidentService.update(READ_ONLY.name, id, createRequest) }
        assertThrowsSuspend<InsufficientRightsException> { accidentService.update(USER.name, id, createRequest) }
        accidentService.update(MODERATOR.name, id, createRequest)
        accidentService.update(ADMIN.name, id, createRequest)
        accidentService.update(SUPER_ADMIN.name, id, createRequest)
        accidentService.update(DEVELOPER.name, id, createRequest)

        assertThrowsSuspend<InsufficientRightsException> { accidentService.setHidden(READ_ONLY.name, id, true) }
        assertThrowsSuspend<InsufficientRightsException> { accidentService.setHidden(USER.name, id, true) }
        accidentService.setHidden(MODERATOR.name, id, true)
        accidentService.setHidden(ADMIN.name, id, true)
        accidentService.setHidden(SUPER_ADMIN.name, id, true)
        accidentService.setHidden(DEVELOPER.name, id, true)

        assertThrowsSuspend<InsufficientRightsException> { accidentService.setConflict(READ_ONLY.name, id, true) }
        assertThrowsSuspend<InsufficientRightsException> { accidentService.setConflict(USER.name, id, true) }
        assertThrowsSuspend<InsufficientRightsException> { accidentService.setConflict(MODERATOR.name, id, true) }
        accidentService.setHidden(ADMIN.name, id, true)
        accidentService.setHidden(SUPER_ADMIN.name, id, true)
        accidentService.setHidden(DEVELOPER.name, id, true)

        Unit
    }

    @Test
    fun `it correctly returns depend of radius`() = runBlocking {
        val accidentService = getService { listOf(accident(), accident(), accident(address = voronezh)) }

        val moscowResult = accidentService.getList(USER.name, 10, null, GeoConstraint.fromAddress(moscow))
        assertEquals(2, moscowResult.size)

        val voronezhResult = accidentService.getList(USER.name, 10, null, GeoConstraint.fromAddress(voronezh))
        assertEquals(1, voronezhResult.size)

        val worldResult = accidentService.getList(USER.name, 10, null, GeoConstraint.fromAddress(voronezh, 100_000))
        assertEquals(3, worldResult.size)
    }

    companion object {
        @Suppress("unused")
        @JvmStatic
        fun hiddenDataset(): Stream<Arguments> = Stream.of(
            Arguments.of(READ_ONLY, 5),
            Arguments.of(USER, 6),
            Arguments.of(MODERATOR, 7),
            Arguments.of(ADMIN, 7),
            Arguments.of(SUPER_ADMIN, 7),
            Arguments.of(DEVELOPER, 7),
        )

        @Suppress("unused")
        @JvmStatic
        fun depthDataset(): Stream<Arguments> = Stream.of(
            Arguments.of(READ_ONLY, 7),
            Arguments.of(USER, 7),
            Arguments.of(MODERATOR, 9),
            Arguments.of(ADMIN, 9),
            Arguments.of(SUPER_ADMIN, 10),
            Arguments.of(DEVELOPER, 10),
        )

        @Suppress("unused")
        @JvmStatic
        fun lastFetchDataset(): Stream<Arguments> = Stream.of(
            Arguments.of(READ_ONLY, 6),
            Arguments.of(USER, 6),
            Arguments.of(MODERATOR, 6),
            Arguments.of(ADMIN, 6),
            Arguments.of(SUPER_ADMIN, 6),
            Arguments.of(DEVELOPER, 6),
        )

        private val current = System.currentTimeMillis()
        private val hoursAgo10 = current - Duration.ofHours(10).toMillis()
        private val hoursAgo15 = current - Duration.ofHours(15).toMillis()
        private val hoursAgo23 = current - Duration.ofHours(23).toMillis()
        private val hoursAgo27 = current - Duration.ofHours(27).toMillis()
        private val hoursAgo100 = current - Duration.ofHours(100).toMillis()

        private val depthTestAccidents = generateSequence { accident() }.take(5).toMutableList().apply {
            add(accident(created = hoursAgo23))
            add(accident(created = hoursAgo100, updated = hoursAgo10))
            //MODERATOR, ADMIN
            add(accident(created = hoursAgo27))
            add(accident(created = hoursAgo100, updated = hoursAgo27))
            //SUPER_MODERATOR, DEVELOPER
            add(accident(created = hoursAgo100))
        }
    }

    private fun getService(accidents: () -> List<Accident>): AccidentService {
        return AccidentService(
            userDataSource,
            NotificatorService(),
            AccidentDataSourceMock.getInstance(accidents())
        )
    }
}