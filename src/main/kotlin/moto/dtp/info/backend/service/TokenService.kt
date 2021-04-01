package moto.dtp.info.backend.service

import moto.dtp.info.backend.rest.request.AuthRequest
import org.springframework.stereotype.Service
import java.util.*

@Service
class TokenService {
    fun createToken(authRequest: AuthRequest) = when (authRequest) {
        AuthRequest.Anonymous -> ANONYMOUS_TOKEN
        is AuthRequest.Basic  -> UUID.randomUUID().toString()
    }

    fun isAnonymous(token: String) = token == ANONYMOUS_TOKEN

    companion object {
        const val ANONYMOUS_TOKEN = "c15aeaac-f7da-4f7c-8441-7c48335f54ac"
    }
}