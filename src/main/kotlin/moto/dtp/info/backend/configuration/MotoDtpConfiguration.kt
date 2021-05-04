package moto.dtp.info.backend.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "motodtp")
data class MotoDtpConfiguration(val security: Security) {
    data class Security(val jwtSecret: String)
}