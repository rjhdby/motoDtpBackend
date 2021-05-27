package moto.dtp.info.backend.rest.handler

import moto.dtp.info.backend.exception.AuthException
import moto.dtp.info.backend.exception.ImpossibleException
import moto.dtp.info.backend.exception.InsufficientRightsException
import moto.dtp.info.backend.exception.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

object ResponseHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun <T> handle(responseProvider: suspend () -> T): ResponseEntity<T> =
        try {
            ResponseEntity.ok(responseProvider())
        } catch (e: InsufficientRightsException) {
            logger.error(e.message)
            ResponseEntity(HttpStatus.FORBIDDEN)
        } catch (e: NotFoundException) {
            logger.error(e.message)
            ResponseEntity(HttpStatus.NOT_FOUND)
        } catch (e: IllegalArgumentException) {
            logger.error(e.message)
            ResponseEntity(HttpStatus.EXPECTATION_FAILED)
        } catch (e: AuthException) {
            logger.error(e.message)
            ResponseEntity(HttpStatus.FORBIDDEN)
        } catch (e: ImpossibleException) {
            logger.error(e.message)
            ResponseEntity(HttpStatus.I_AM_A_TEAPOT)
        }
}