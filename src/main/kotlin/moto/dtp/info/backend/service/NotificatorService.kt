package moto.dtp.info.backend.service

import moto.dtp.info.backend.domain.accident.Accident
import org.springframework.stereotype.Service

@Service
class NotificatorService {
    fun notifyCreated(accident: Accident) {
        Unit
    }

    fun notifyConflict(accident: Accident) {
        Unit
    }

    fun notifyConflictCanceled(accident: Accident) {
        Unit
    }
}