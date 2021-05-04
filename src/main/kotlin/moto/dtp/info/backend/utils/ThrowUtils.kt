package moto.dtp.info.backend.utils

object ThrowUtils {
    fun throwInternal(message: String? = null): Nothing {
        throw InternalError(message ?: "Something went terrible wrong")
    }
}