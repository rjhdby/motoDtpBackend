package moto.dtp.info.backend.rest.request

sealed class AuthRequest {
    object Anonymous : AuthRequest()
    data class Basic(val login: String, val password: String, val nick: String? = null) : AuthRequest()
    data class VK(val code: String) : AuthRequest()
}
