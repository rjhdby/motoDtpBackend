package mock

import moto.dtp.info.backend.datasources.AccidentDataSource
import moto.dtp.info.backend.domain.accident.Accident
import moto.dtp.info.backend.domain.accident.AccidentType
import moto.dtp.info.backend.domain.accident.Address
import org.bson.types.ObjectId
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.kotlin.any
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.mock

object AccidentDataSourceMock {
    fun getInstance(initialContext: List<Accident>): AccidentDataSource {
        val current = System.currentTimeMillis()
        val twoDaysAgo = current - 172_800_000L
        val hourAgo = current - 3_600_000L
        val context = if (initialContext.isNullOrEmpty()) {
            listOf(
                accident(),
                accident(address = voronezh),
                accident(hidden = true),
                accident(created = twoDaysAgo),
                accident(updated = hourAgo),
                accident(created = twoDaysAgo, updated = hourAgo),
            )
        } else {
            initialContext
        }

        return mock {
            onBlocking { get(any()) } doSuspendableAnswer { args -> context.firstOrNull { it.id == ObjectId(args.arguments[0] as String) } }
            onBlocking { persist(any()) } doSuspendableAnswer { it.arguments[0] as Accident }
            onBlocking { getListFrom(anyLong()) } doSuspendableAnswer { args -> context.filter { it.updated > (args.arguments[0] as Long) } }
            onBlocking { findOneFrom(any(), anyLong()) } doSuspendableAnswer { args ->
                context.firstOrNull { it.id == ObjectId(args.arguments[0] as String) && it.updated > (args.arguments[1] as Long) }
            }
        }
    }

    fun accident(
        created: Long = System.currentTimeMillis(),
        updated: Long = created,
        creator: ObjectId = ObjectId(),
        address: Address = moscow,
        hidden: Boolean = false
    ) = Accident(ObjectId(), AccidentType.MOTO_AUTO, created, creator, address, updated, hidden = hidden)

    val moscow = Address(lat = 55.751244f, lon = 37.618423f, address = "Moscow")
    val voronezh = Address(lat = 51.672044f, lon = 39.18433f, address = "Voronezh")
}