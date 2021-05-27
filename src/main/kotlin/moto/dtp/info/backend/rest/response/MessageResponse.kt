package moto.dtp.info.backend.rest.response

import java.util.*

class MessageResponse(
    val id: String,
    val author: String,
    val authorNick: String,
    val topic: String,
    val created: Date,
    var hidden: Boolean = false,
    var text: String
)