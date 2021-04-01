package moto.dtp.info.backend.service

import moto.dtp.info.backend.rest.request.AuthRequest
import org.springframework.stereotype.Service

@Service
class AuthRequestValidator {
    suspend fun validate(authRequest: AuthRequest) {
        when (authRequest) {
            AuthRequest.Anonymous -> return
            is AuthRequest.Basic  -> validateBasic(authRequest)
        }
    }

    private suspend fun validateBasic(authRequest: AuthRequest.Basic) {
        when {
            authRequest.login.isEmpty()     -> throw IllegalArgumentException("Login is empty")
            authRequest.password.isEmpty()  -> throw IllegalArgumentException("Password is empty")
            authRequest.password.length < 6 -> throw IllegalArgumentException("Password is too short")
        }
    }
}