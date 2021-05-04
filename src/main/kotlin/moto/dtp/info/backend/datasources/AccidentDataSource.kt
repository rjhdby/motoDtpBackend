package moto.dtp.info.backend.datasources

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import moto.dtp.info.backend.domain.accident.Accident
import moto.dtp.info.backend.repository.AccidentRepository
import moto.dtp.info.backend.utils.TimeUtils
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import kotlin.contracts.contract

@Service
class AccidentDataSource(
    private val accidentRepository: AccidentRepository
) {
    private val cache: Cache<String, Accident> = Cache(500) { entry: Accident, currentTime: Long ->
        entry.created < currentTime - TTL
    }

    init {
        runBlocking {
            getListFrom(System.currentTimeMillis() - TTL - 1)
        }
    }

    suspend fun get(id: String): Accident? {
        val cached = cache.get(id)
        if (cached != null) {
            return cached
        }

        val persisted = accidentRepository.findById(ObjectId(id)).awaitFirstOrNull()
        if (persisted != null) {
            cache.put(persisted.id!!.toHexString(), persisted)
        }

        return persisted
    }

    suspend fun persist(accident: Accident): Accident {
        val persisted = accidentRepository.save(accident).awaitFirst()
        cache.put(persisted.id!!.toHexString(), persisted)

        return persisted
    }

    suspend fun getListFrom(from: Long): List<Accident> {
        if (from > System.currentTimeMillis() - TTL) {
            return cache.entries().map { it.value }.filter { it.created > from }
        }

        val persisted = accidentRepository.findFrom(from).asFlow().toList()
        persisted.forEach {
            cache.put(it.id!!.toHexString(), it)
        }

        return persisted
    }

    suspend fun findOneFrom(id: String, from: Long): Accident? {
        return get(id).takeIf { it?.created ?: 0 > from }
    }

    companion object {
        private const val TTL = TimeUtils.DAY * 2
    }
}