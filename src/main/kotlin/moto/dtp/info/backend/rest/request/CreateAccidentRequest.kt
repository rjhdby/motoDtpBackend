package moto.dtp.info.backend.rest.request

import moto.dtp.info.backend.domain.accident.AccidentHardness
import moto.dtp.info.backend.domain.accident.AccidentType
import moto.dtp.info.backend.domain.accident.Address

data class CreateAccidentRequest(
    val type: AccidentType,
    val location: Address,
    val description: String,
    val hardness: AccidentHardness? = null,
)