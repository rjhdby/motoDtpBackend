package integration.moto.dtp.info.backend.datasources

import kotlinx.coroutines.runBlocking
import mock.AccidentDataSourceMock.moscow
import moto.dtp.info.backend.MotoDtpBackendApplication
import moto.dtp.info.backend.domain.accident.AccidentType
import moto.dtp.info.backend.domain.user.UserRole
import moto.dtp.info.backend.rest.request.AuthRequest
import moto.dtp.info.backend.rest.request.CreateAccidentRequest
import moto.dtp.info.backend.service.AccidentService
import moto.dtp.info.backend.service.MessageService
import moto.dtp.info.backend.service.UserService
import org.junit.ClassRule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.junit4.SpringRunner
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName.parse
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [MotoDtpBackendApplication::class])
@RunWith(SpringRunner::class)
@Disabled
internal class MessagesServiceTest {
    @Autowired
    private lateinit var messagesService: MessageService

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var accidentService: AccidentService

    @ClassRule
    private val mongoDBContainer = MongoDBContainer(parse("mongo:4.0.10")).also {
        it.start()
        System.setProperty("MONGODB", it.replicaSetUrl)
    }

    @Test
    fun `happy path`() = runBlocking {
        val token = userService.register(AuthRequest.Basic("test", "123456"))
        val token2 = userService.register(AuthRequest.Basic("test", "123456"))
        assertEquals(token, token2)

        val user = userService.getUserByToken(token)
        assertEquals("test", user.nick)
        assertEquals(UserRole.USER, user.role)

        val accident = accidentService.create(token, CreateAccidentRequest(AccidentType.MOTO_AUTO, moscow, ""))
        assertEquals(accident.creator, user.id)

        val message = messagesService.create(token, accident.id!!.toHexString(), "test")
        assertEquals("test", message.text)
        assertEquals(accident.id, message.topic)
        assertEquals(user.id, message.author)

        val messageList = messagesService.getList(token, accident.id!!.toHexString())
        assertEquals(1, messageList.size)
    }
}