package moto.dtp.info.backend.domain.user

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import moto.dtp.info.backend.domain.EntityWithId
import org.bson.types.ObjectId
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class User(
    override val id: ObjectId? = null,
    val created: Long = System.currentTimeMillis(),
    val nick: String,
    var role: UserRole = UserRole.USER
) : EntityWithId {
    companion object {
        private const val ANONYMOUS_NICK = "Anonymous"
        val ANONYMOUS_ID: ObjectId = ObjectId.getSmallestWithDate(Date(0))
        val ANONYMOUS = User(id = ANONYMOUS_ID, nick = ANONYMOUS_NICK, role = UserRole.READ_ONLY)
    }
}
