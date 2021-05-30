package moto.dtp.info.backend

import moto.dtp.info.backend.configuration.MotoDtpConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableConfigurationProperties(MotoDtpConfiguration::class)
@EnableScheduling
class MotoDtpBackendApplication

fun main(args: Array<String>) {
    runApplication<MotoDtpBackendApplication>(*args)
}
