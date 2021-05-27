package moto.dtp.info.backend.rest.v1

import kotlinx.coroutines.reactor.mono
import moto.dtp.info.backend.domain.message.Message
import moto.dtp.info.backend.rest.converter.MessageConverter
import moto.dtp.info.backend.rest.handler.ResponseHandler.handle
import moto.dtp.info.backend.rest.response.MessageResponse
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
    private val messageConverter: MessageConverter
) {
    @GetMapping(value = ["/{topic}"])
    fun list(
        @RequestHeader("token") token: String,
        @PathVariable topic: String
    ): Mono<ResponseEntity<List<MessageResponse>>> = mono { messageService.getList(token, topic).toResponse() }

    @PostMapping(value = ["/{topic}"])
    fun create(
        @RequestHeader("token") token: String,
        @PathVariable topic: String,
        @RequestParam text: String
    ): Mono<ResponseEntity<MessageResponse>> = mono { messageService.create(token, topic, text).toResponse() }

    @PutMapping(value = ["/{id}/hide"])
    fun hide(
        @RequestHeader("token") token: String,
        @PathVariable id: String,
    ): Mono<ResponseEntity<MessageResponse>> = mono { messageService.setHidden(token, id).toResponse() }

    @PutMapping(value = ["/{id}/show"])
    fun show(
        @RequestHeader("token") token: String,
        @PathVariable id: String,
    ): Mono<ResponseEntity<MessageResponse>> = mono { messageService.resetHidden(token, id).toResponse() }

    @PutMapping(value = ["/{id}"])
    fun modifyText(
        @RequestHeader("token") token: String,
        @PathVariable id: String,
        @RequestParam text: String
    ): Mono<ResponseEntity<MessageResponse>> = mono { messageService.modifyText(token, id, text).toResponse() }

    private suspend fun Message.toResponse() = handle { messageConverter.toMessageResponse(this) }

    private suspend fun Iterable<Message>.toResponse() = handle { map { messageConverter.toMessageResponse(it) } }
}