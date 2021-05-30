package moto.dtp.info.backend.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "motodtp")
data class MotoDtpConfiguration(
    val security: Security,
    val vk: Vk,
    val nomination: Nomination
) {
    data class Security(val jwtSecret: String)

    data class Vk(
        val appId: Int,
        val apiVersion: String,
        val redirectUrl: String,
        val tokenUrl: String,
        val dataUrl: String,
        val secret: String
    )

    data class Nomination(val url: String)
}