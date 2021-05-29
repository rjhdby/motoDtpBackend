import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions

inline fun <reified T : Throwable> assertThrowsSuspend(crossinline executable: suspend () -> Unit) {
    Assertions.assertThrows(T::class.java) {
        runBlocking { executable() }
    }
}