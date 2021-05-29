package mock

import moto.dtp.info.backend.domain.user.User
import moto.dtp.info.backend.domain.user.UserRole
import moto.dtp.info.backend.rest.request.AuthRequest
import moto.dtp.info.backend.service.UserService
import org.bson.types.ObjectId
import org.mockito.kotlin.any
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.mock

object UserServiceMock {
    fun getInstance(): UserService {
        val userDataSource = UserDataSourceMock.getInstance()
        return mock {
            onBlocking { getUserByToken(any()) } doSuspendableAnswer { userDataSource.getByToken(it.arguments[0] as String) }
            onBlocking { getUser(any()) } doSuspendableAnswer { userDataSource.get(it.arguments[0] as ObjectId) }
            onBlocking { invalidate(any()) } doSuspendableAnswer { userDataSource.get(ObjectId(it.arguments[0] as String)) }
            onBlocking { persist(any()) } doSuspendableAnswer { userDataSource.persist(it.arguments[0] as User) }
            onBlocking { register(any()) } doSuspendableAnswer {
                when (it.arguments[0]) {
                    is AuthRequest.Anonymous -> UserRole.READ_ONLY.name
                    else                     -> UserRole.USER.name
                }
            }
        }
    }
}