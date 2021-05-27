package moto.dtp.info.backend.service

import kotlinx.coroutines.runBlocking
import moto.dtp.info.backend.domain.user.User
import moto.dtp.info.backend.domain.user.UserRole
import moto.dtp.info.backend.domain.user.UserRole.*
import moto.dtp.info.backend.exception.InsufficientRightsException
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock

internal class ModeratorServiceTest {
    @BeforeEach
    fun setUp() {
        resetUsers()
    }

    @Test
    fun `READ_ONLY can't manage roles`() {
        val admin = users[READ_ONLY]!!
        UserRole.values().forEach { source ->
            UserRole.values().forEach { target ->
                resetUsers()
                assertThrowsSuspend<InsufficientRightsException> {
                    moderatorService.setRole(admin.role.name, uid(source), target)
                }
            }
        }
    }

    @Test
    fun `USER can't manage roles`() {
        val admin = users[USER]!!
        UserRole.values().forEach { source ->
            UserRole.values().forEach { target ->
                resetUsers()
                assertThrowsSuspend<InsufficientRightsException> {
                    moderatorService.setRole(admin.role.name, uid(source), target)
                }
            }
        }
    }

    @Test
    fun `MODERATOR can manage USER and less`() {
        val admin = users[MODERATOR]!!
        UserRole.values().forEach { source ->
            UserRole.values().forEach { target ->
                resetUsers()
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
    fun `ADMIN can manage MODERATOR and less`() {
        val admin = users[ADMIN]!!
        UserRole.values().forEach { source ->
            UserRole.values().forEach { target ->
                resetUsers()
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
    fun `SUPER_ADMIN can manage ADMIN and less`() {
        val admin = users[SUPER_ADMIN]!!
        UserRole.values().forEach { source ->
            UserRole.values().forEach { target ->
                resetUsers()
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
    fun `DEVELOPER can manage ADMIN and less`() {
        val admin = users[DEVELOPER]!!
        UserRole.values().forEach { source ->
            UserRole.values().forEach { target ->
                resetUsers()
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
    fun `no one can manage SUPER_ADMIN`() {
        val admin = users[SUPER_ADMIN]!!
        UserRole.values().forEach { source ->
            UserRole.values().forEach { target ->
                resetUsers()
                assertThrowsSuspend<InsufficientRightsException> {
                    moderatorService.setRole(source.name, uid(admin.role), target)
                }
            }
        }
    }

    @Test
    fun `no one can manage DEVELOPER`() {
        val admin = users[DEVELOPER]!!
        UserRole.values().forEach { source ->
            UserRole.values().forEach { target ->
                resetUsers()
                assertThrowsSuspend<InsufficientRightsException> {
                    moderatorService.setRole(source.name, uid(admin.role), target)
                }
            }
        }
    }

    private val moderatorService = ModeratorService(mock {
        onBlocking {
            getUserByToken(any())
        } doAnswer {
            users[UserRole.valueOf(it.arguments[0] as String)]
        }

        onBlocking {
            getUser(any())
        } doAnswer { args ->
            users.values.first { it.id == (args.arguments[0] as ObjectId) }
        }
    })

    private inline fun <reified T : Throwable> assertThrowsSuspend(crossinline executable: suspend () -> Unit) {
        assertThrows(T::class.java) {
            runBlocking { executable() }
        }
    }

    private fun resetUsers() {
        users = mapOf(
            READ_ONLY to User(id = ObjectId(), nick = READ_ONLY.name, role = READ_ONLY),
            USER to User(id = ObjectId(), nick = USER.name, role = USER),
            MODERATOR to User(id = ObjectId(), nick = MODERATOR.name, role = MODERATOR),
            ADMIN to User(id = ObjectId(), nick = ADMIN.name, role = ADMIN),
            SUPER_ADMIN to User(id = ObjectId(), nick = SUPER_ADMIN.name, role = SUPER_ADMIN),
            DEVELOPER to User(id = ObjectId(), nick = DEVELOPER.name, role = DEVELOPER),
        )
    }

    companion object {
        @Volatile
        private var users = mapOf<UserRole, User>()

        private fun uid(role: UserRole) = users[role]!!.id!!.toHexString()
    }
}