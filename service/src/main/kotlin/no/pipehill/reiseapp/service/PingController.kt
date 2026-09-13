package no.pipehill.reiseapp.service

import no.pipehill.reiseapp.api.PingApi
import no.pipehill.reiseapp.api.dto.PingResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class PingController : PingApi {
    override fun ping(): ResponseEntity<PingResponse> =
        ResponseEntity.ok(PingResponse(message = "pong"))
}
