package no.pipehill.reiseapp.service

import no.pipehill.reiseapp.api.HealthApi
import no.pipehill.reiseapp.api.dto.HealthResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController : HealthApi {
    override fun health(): ResponseEntity<HealthResponse> =
        ResponseEntity.ok(HealthResponse(status = HealthResponse.Status.UP))
}
