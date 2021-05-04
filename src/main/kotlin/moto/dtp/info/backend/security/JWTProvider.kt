package moto.dtp.info.backend.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.impl.DefaultClaims
import moto.dtp.info.backend.configuration.MotoDtpConfiguration
import moto.dtp.info.backend.domain.user.User
import moto.dtp.info.backend.domain.user.User.Companion.ANONYMOUS_ID
import moto.dtp.info.backend.exception.AuthException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class JWTProvider(
    motoDtpConfiguration: MotoDtpConfiguration
) {
    private val jwtSecret = motoDtpConfiguration.security.jwtSecret
    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun createToken(user: User): String {
        val builder = Jwts.builder()
        builder.claim(ID_CLAIM, user.id?.toHexString() ?: ANONYMOUS_ID.toHexString())
        return builder
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact()
    }

    fun extractUserId(token: String): String = try {
        val claims = Jwts.parser().setSigningKey(jwtSecret).parse(token).body as DefaultClaims
        claims.get(ID_CLAIM, String::class.java) ?: throw AuthException()
    } catch (e: Exception) {
        logger.error(e.message)
        throw AuthException()
    }

    companion object {
        private const val ID_CLAIM = "id"
    }
}