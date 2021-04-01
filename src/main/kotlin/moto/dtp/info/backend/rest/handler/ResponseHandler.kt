package moto.dtp.info.backend.rest.handler

import moto.dtp.info.backend.exception.InsufficientRightsException
import moto.dtp.info.backend.exception.NotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

object ResponseHandler {
    suspend fun <T> handle(responseProvider: suspend () -> T): ResponseEntity<T> =
        try {
            ResponseEntity.ok(responseProvider())
        } catch (e: InsufficientRightsException) {
            ResponseEntity(HttpStatus.FORBIDDEN)
        } catch (e: NotFoundException) {
            ResponseEntity(HttpStatus.NOT_FOUND)
        } catch (e: IllegalArgumentException) {
            ResponseEntity(HttpStatus.EXPECTATION_FAILED)
        }
}