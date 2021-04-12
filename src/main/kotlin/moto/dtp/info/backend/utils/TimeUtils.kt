package moto.dtp.info.backend.utils

object TimeUtils {
    fun currentSec() = System.currentTimeMillis() / 1000

    const val SECOND = 1000L
    const val MINUTE = SECOND * 60
    const val HOUR = MINUTE * 60
    const val DAY = HOUR * 24
}