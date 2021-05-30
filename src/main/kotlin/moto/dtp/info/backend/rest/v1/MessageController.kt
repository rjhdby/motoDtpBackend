package moto.dtp.info.backend.rest.v1

import io.swagger.v3.oas.annotations.Operation
import kotlinx.coroutines.reactor.mono
import moto.dtp.info.backend.domain.message.Message
import moto.dtp.info.backend.rest.Versions
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
    @Operation(tags = ["Messages API"], summary = "Get a list of messages for the topic(accident)")
    @GetMapping(value = ["/{topic}"])
    fun list(
        @RequestHeader("token") token: String,
        @PathVariable topic: String
    ): Mono<ResponseEntity<List<MessageResponse>>> = listResponse { messageService.getList(token, topic) }

    @Operation(tags = ["Messages API"], summary = "Create new message")
    @PostMapping(value = ["/{topic}"])
    fun create(
        @RequestHeader("token") token: String,
        @PathVariable topic: String,
        @RequestParam text: String
    ): Mono<ResponseEntity<MessageResponse>> = response { messageService.create(token, topic, text) }

    @Operation(tags = ["Messages API"], summary = "Hide a message")
    @PutMapping(value = ["/{id}/hide"])
    fun hide(
        @RequestHeader("token") token: String,
        @PathVariable id: String,
    ): Mono<ResponseEntity<MessageResponse>> = response { messageService.setHidden(token, id) }

    @Operation(tags = ["Messages API"], summary = "Show a previously hidden message")
    @PutMapping(value = ["/{id}/show"])
    fun show(
        @RequestHeader("token") token: String,
        @PathVariable id: String,
    ): Mono<ResponseEntity<MessageResponse>> = response { messageService.resetHidden(token, id) }

    @Operation(tags = ["Messages API"], summary = "Update a message")
    @PutMapping(value = ["/{id}"])
    fun modifyText(
        @RequestHeader("token") token: String,
        @PathVariable id: String,
        @RequestParam text: String
    ): Mono<ResponseEntity<MessageResponse>> = response { messageService.modifyText(token, id, text) }

    private fun response(provider: suspend () -> Message) =
        mono { handle { messageConverter.toMessageResponse(provider()) } }

    private fun listResponse(provider: suspend () -> Iterable<Message>) =
        mono { handle { provider().map { messageConverter.toMessageResponse(it) } } }
}