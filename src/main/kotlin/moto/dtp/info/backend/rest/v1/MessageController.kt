package moto.dtp.info.backend.rest.v1

import kotlinx.coroutines.reactor.mono
import moto.dtp.info.backend.domain.message.Message
import moto.dtp.info.backend.rest.handler.ResponseHandler.handle
import moto.dtp.info.backend.service.MessageService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
@RestController
@RequestMapping(value = ["${Versions.V1}/message"])
class MessageController(
    private val messageService: MessageService,
) {
    @GetMapping(value = ["/{topic}"])
    fun list(
        @RequestHeader("token") token: String,
        @PathVariable topic: String
    ): Mono<ResponseEntity<List<Message>>> = mono { handle { messageService.getList(token, topic) } }

    @PostMapping(value = ["/{topic}"])
    fun create(
        @RequestHeader("token") token: String,
        @PathVariable topic: String,
        @RequestParam text: String
    ): Mono<ResponseEntity<Message>> = mono { handle { messageService.create(token, topic, text) } }

    @PutMapping(value = ["/{id}/hide"])
    fun hide(
        @RequestHeader("token") token: String,
        @PathVariable id: String,
    ): Mono<ResponseEntity<Message>> = mono { handle { messageService.setHidden(token, id, true) } }

    @PutMapping(value = ["/{id}/show"])
    fun show(
        @RequestHeader("token") token: String,
        @PathVariable id: String,
    ): Mono<ResponseEntity<Message>> = mono { handle { messageService.setHidden(token, id, false) } }

    @PutMapping(value = ["/{id}"])
    fun modifyText(
        @RequestHeader("token") token: String,
        @PathVariable id: String,
        @RequestParam text: String
    ): Mono<ResponseEntity<Message>> = mono { handle { messageService.modifyText(token, id, text) } }
}