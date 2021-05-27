package moto.dtp.info.backend.datasources

import java.util.concurrent.ConcurrentHashMap

class Cache<KEY : Any, ENTRY : Any>(
    estimatedSize: Int,
    private val expiredPredicate: (entry: ENTRY, currentTime: Long) -> Boolean
) {
    private val cache: ConcurrentHashMap<KEY, ENTRY> = ConcurrentHashMap(estimatedSize / 2, 0.75f, 2)

    fun put(key: KEY, entry: ENTRY) {
        expire()
        cache[key] = entry
    }

    fun get(key: KEY): ENTRY? {
        expire()
        val entry = cache[key]
        if (entry != null) {
            put(key, entry)
        }

        return entry
    }

    fun entries() = cache.entries

    private fun expire() {
        val current = System.currentTimeMillis()
        cache.entries.removeIf { expiredPredicate(it.value, current) }
    }
}