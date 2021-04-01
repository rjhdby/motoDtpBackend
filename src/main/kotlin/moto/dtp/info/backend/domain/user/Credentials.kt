package moto.dtp.info.backend.domain.user

import moto.dtp.info.backend.rest.request.AuthRequest
import org.springframework.util.DigestUtils
import java.util.*

data class Credentials(val login: String, val passHash: String, val salt: String) {

    fun guardPassword(password: String) {
        if (passHash != hashPassword(password, salt)) {
            throw SecurityException("Wrong password")
        }
    }

    companion object {
        fun fromRequest(request: AuthRequest.Basic): Credentials {
            val salt = generateSalt()
            return Credentials(request.login, hashPassword(request.password, salt), salt)
        }

        private fun hashPassword(password: String, salt: String): String {
            val inner = DigestUtils.md5DigestAsHex(salt.toByteArray())

            return DigestUtils.md5DigestAsHex("$inner$password".toByteArray())
        }

        private fun generateSalt(): String = UUID.randomUUID().toString().take(4)
    }
}