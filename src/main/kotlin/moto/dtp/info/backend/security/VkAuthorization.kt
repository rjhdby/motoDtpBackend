package moto.dtp.info.backend.security

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import moto.dtp.info.backend.configuration.MotoDtpConfiguration
import moto.dtp.info.backend.domain.user.Auth
import moto.dtp.info.backend.domain.user.AuthType
import moto.dtp.info.backend.domain.user.User
import moto.dtp.info.backend.domain.user.VkUser
import moto.dtp.info.backend.repository.AuthRepository
import moto.dtp.info.backend.repository.UserRepository
import moto.dtp.info.backend.rest.request.AuthRequest
import moto.dtp.info.backend.utils.ThrowUtils.throwInternal
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class VkAuthorization(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    motoDtpConfiguration: MotoDtpConfiguration
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val vk = motoDtpConfiguration.vk
    private val restTemplate = RestTemplate()

    private val tokenUrl =
        "${vk.tokenUrl}?client_id=${vk.appId}&client_secret=${vk.secret}&redirect_uri=${vk.redirectUrl}&code={code}"
    private val dataUrl =
        "${vk.dataUrl}?access_token={token}&user_ids={user}&v=${vk.apiVersion}"

    suspend fun authorize(authRequest: AuthRequest.VK): User {
        val vkUser = retrieveVkUser(authRequest.code)
        val auth = authRepository.findByVkId(vkUser.id).awaitFirstOrNull()
        if (auth != null) {
            return userRepository.findById(auth.uid).awaitFirst()
        }
        val user = userRepository.save(User(nick = vkUser.nick)).awaitFirst()
        authRepository.save(Auth(uid = user.id!!, type = AuthType.VK, vk = vkUser)).awaitFirst()

        return user
    }

    private suspend fun retrieveVkUser(code: String): VkUser {
        try {
            val accessToken = restTemplate.getForObject(
                tokenUrl,
                VkTokenResponse::class.java,
                code
            ) ?: throwInternal("NULL result for VK access token")

            val userInfo = restTemplate.getForObject(
                dataUrl,
                VkUserDataResponse::class.java,
                accessToken.token,
                accessToken.userId
            )?.response ?: throwInternal("NULL result for VK user info")

            if (userInfo.isNullOrEmpty()) {
                throwInternal("NULL result for VK user info")
            }
            val nick = listOfNotNull(userInfo[0].lastName, userInfo[0].lastName)
                .joinToString(" ")
                .ifBlank { accessToken.userId.toString() }

            return VkUser(nick, accessToken.userId)
        } catch (e: Exception) {
            logger.error(e.message)
            throw e
        }
    }

    @ConstructorBinding
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class VkTokenResponse(
        @JsonProperty(value = "access_token") val token: String,
        @JsonProperty(value = "user_id") val userId: Int
    )

    @ConstructorBinding
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class VkUserDataResponse(
        val response: List<UserData>
    ) {
        @ConstructorBinding
        @JsonIgnoreProperties(ignoreUnknown = true)
        data class UserData(
            @JsonProperty(value = "first_name") val firstName: String?,
            @JsonProperty(value = "last_name") val lastName: String?,
        )
    }
}