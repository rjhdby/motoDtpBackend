package mock

import moto.dtp.info.backend.datasources.UserDataSource
import moto.dtp.info.backend.domain.user.User
import moto.dtp.info.backend.domain.user.UserRole
import org.bson.types.ObjectId
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock

object UserDataSourceMock {
    fun getInstance(): UserDataSource {
        val context = UserRole.values().associateWith {
            User(id = ObjectId(), nick = it.name, role = it)
        }
        return mock {
            onBlocking { getByToken(any()) } doAnswer { context[UserRole.valueOf(it.arguments[0] as String)] }
            onBlocking { persist(any()) } doAnswer { it.arguments[0] as User }
            onBlocking { get(any()) } doAnswer { args -> context.values.first { it.id == (args.arguments[0] as ObjectId) } }
            onBlocking { invalidateAndGet(any()) } doAnswer { args -> context.values.first { it.id == (args.arguments[0] as ObjectId) } }
        }
    }
}