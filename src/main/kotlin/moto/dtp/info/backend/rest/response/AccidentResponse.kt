package moto.dtp.info.backend.rest.response

import com.fasterxml.jackson.annotation.JsonInclude
import moto.dtp.info.backend.domain.accident.AccidentHardness
import moto.dtp.info.backend.domain.accident.AccidentType
import moto.dtp.info.backend.domain.accident.Address
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AccidentResponse(
    val id: String?,
    val created: Date,
    val type: AccidentType,
    val resolved: Date?,
    val verified: Boolean,
    val hidden: Boolean,
    val hardness: AccidentHardness,
    val creator: String,
    val location: Address,
    val description: String,
    val conflict: Boolean
)
