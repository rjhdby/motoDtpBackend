package moto.dtp.info.backend.service

import assertThrowsSuspend
import kotlinx.coroutines.runBlocking
import mock.UserServiceMock
import moto.dtp.info.backend.domain.user.UserRole
import moto.dtp.info.backend.domain.user.UserRole.*
import moto.dtp.info.backend.exception.InsufficientRightsException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ModeratorServiceTest {
    private var userService = UserServiceMock.getInstance()
    private var moderatorService = ModeratorService(userService)

    @BeforeEach
    fun setUp() {
        userService = UserServiceMock.getInstance()
        moderatorService = ModeratorService(userService)
    }

    @Test
    fun `READ_ONLY can't manage roles`() = runBlocking {
        val admin = userService.getUserByToken(READ_ONLY.name)
        UserRole.values().forEach { source ->
            UserRole.values().forEach { target ->
                assertThrowsSuspend<InsufficientRightsException> {
                    moderatorService.setRole(admin.role.name, uid(source), target)
                }
            }
        }
    }

    @Test
    fun `USER can't manage roles`() = runBlocking {
        val admin = userService.getUserByToken(USER.name)
        UserRole.values().forEach { source ->
            UserRole.values().forEach { target ->
                assertThrowsSuspend<InsufficientRightsException> {
                    moderatorService.setRole(admin.role.name, uid(source), target)
                }
            }
        }
    }

    @Test
    fun `MODERATOR can manage USER and less`() = runBlocking {
        val admin = userService.getUserByToken(MODERATOR.name)
        UserRole.values().forEach { source ->
            UserRole.values().forEach { target ->
                if (source.ordinal <= USER.ordinal && target.ordinal <= USER.ordinal) {
                    runBlocking { moderatorService.setRole(admin.role.name, uid(source), target) }
                } else {
                    assertThrowsSuspend<InsufficientRightsException> {
                        moderatorService.setRole(admin.role.name, uid(source), target)
                    }
                }
            }
        }
    }

    @Test
    fun `ADMIN can manage MODERATOR and less`() = runBlocking {
        val admin = userService.getUserByToken(ADMIN.name)
        UserRole.values().forEach { source ->
            UserRole.values().forEach { target ->
                if (source.ordinal <= MODERATOR.ordinal && target.ordinal <= MODERATOR.ordinal) {
                    runBlocking { moderatorService.setRole(admin.role.name, uid(source), target) }
                } else {
                    assertThrowsSuspend<InsufficientRightsException> {
                        moderatorService.setRole(admin.role.name, uid(source), target)
                    }
                }
            }
        }
    }

    @Test
    fun `SUPER_ADMIN can manage ADMIN and less`() = runBlocking {
        val admin = userService.getUserByToken(SUPER_ADMIN.name)
        UserRole.values().forEach { source ->
            UserRole.values().forEach { target ->
                if (source.ordinal <= ADMIN.ordinal && target.ordinal <= ADMIN.ordinal) {
                    runBlocking { moderatorService.setRole(admin.role.name, uid(source), target) }
                } else {
                    assertThrowsSuspend<InsufficientRightsException> {
                        moderatorService.setRole(admin.role.name, uid(source), target)
                    }
                }
            }
        }
    }

    @Test
    fun `DEVELOPER can manage ADMIN and less`() = runBlocking {
        val admin = userService.getUserByToken(DEVELOPER.name)
        UserRole.values().forEach { source ->
            UserRole.values().forEach { target ->
                if (source.ordinal <= ADMIN.ordinal && target.ordinal <= ADMIN.ordinal) {
                    runBlocking { moderatorService.setRole(admin.role.name, uid(source), target) }
                } else {
                    assertThrowsSuspend<InsufficientRightsException> {
                        moderatorService.setRole(admin.role.name, uid(source), target)
                    }
                }
            }
        }
    }

    @Test
    fun `no one can manage SUPER_ADMIN`() = runBlocking {
        val admin = userService.getUserByToken(SUPER_ADMIN.name)
        UserRole.values().forEach { source ->
            UserRole.values().forEach { target ->
                assertThrowsSuspend<InsufficientRightsException> {
                    moderatorService.setRole(source.name, uid(admin.role), target)
                }
            }
        }
    }

    @Test
    fun `no one can manage DEVELOPER`() = runBlocking {
        val admin = userService.getUserByToken(DEVELOPER.name)
        UserRole.values().forEach { source ->
            UserRole.values().forEach { target ->
                assertThrowsSuspend<InsufficientRightsException> {
                    moderatorService.setRole(source.name, uid(admin.role), target)
                }
            }
        }
    }

    private fun uid(role: UserRole) = runBlocking { userService.getUserByToken(role.name).id!!.toHexString() }
}