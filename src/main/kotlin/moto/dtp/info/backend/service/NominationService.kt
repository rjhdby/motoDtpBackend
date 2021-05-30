package moto.dtp.info.backend.service

import kotlinx.coroutines.*
import moto.dtp.info.backend.datasources.NominationClient
import moto.dtp.info.backend.datasources.NominationClient.NominationResult
import moto.dtp.info.backend.domain.accident.GeoPoint
import moto.dtp.info.backend.exception.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

@Service
class NominationService(
    private val nominationClient: NominationClient
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val jobs = ConcurrentHashMap<String, GeoPoint>()
    private val queue = ConcurrentLinkedQueue<String>()
    private val results = ConcurrentHashMap<GeoPoint, Pair<NominationResult, Long>>()

    fun addJob(token: String, geoPoint: GeoPoint) {
        jobs[token] = geoPoint
        queue.add(token)
        logger.debug("Nomination job added: $token, $geoPoint\nQueue size: ${queue.size}\nJobs size: ${jobs.size}")
    }

    suspend fun retrieveResultAsync(geoPoint: GeoPoint): Deferred<NominationResult> {
        return GlobalScope.async {
            repeat(50) {
                val result = results[geoPoint]
                if (result != null) {
                    results.remove(geoPoint)
                    return@async result.first
                }
                delay(100)
            }

            throw NotFoundException()
        }
    }

    private suspend fun callNomination(geoPoint: GeoPoint) {
        results[geoPoint] = Pair(nominationClient.resolve(geoPoint), System.currentTimeMillis() + TTL)
    }

    fun cleanUpResults() {
        val current = System.currentTimeMillis()
        results.entries.removeIf { it.value.second < current }
    }

    @Scheduled(fixedRate = 500L)
    private fun workCycle() {
        cleanUpResults()
        var geoPoint: GeoPoint?
        while (true) {
            val token = queue.poll() ?: return
            geoPoint = jobs[token]
            if (geoPoint != null) {
                jobs.remove(token)
                break
            }
        }

        GlobalScope.launch(Dispatchers.IO) {
            callNomination(geoPoint!!)
        }
    }

    companion object {
        private const val TTL = 10_000L
    }
}