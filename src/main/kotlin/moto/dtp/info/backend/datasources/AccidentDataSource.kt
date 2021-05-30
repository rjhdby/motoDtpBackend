package moto.dtp.info.backend.datasources

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import moto.dtp.info.backend.domain.accident.Accident
import moto.dtp.info.backend.repository.AccidentRepository
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class AccidentDataSource(
    private val accidentRepository: AccidentRepository
) {
    suspend fun get(id: String): Accident? = accidentRepository.findById(ObjectId(id)).awaitFirstOrNull()

    suspend fun persist(accident: Accident): Accident = accidentRepository.save(accident).awaitFirst()

    suspend fun getListFrom(from: Long): List<Accident> = accidentRepository.findFrom(from).asFlow().toList()

    suspend fun findOneFrom(id: String, from: Long): Accident? {
        return get(id).takeIf { it?.created ?: 0 > from }
    }
}