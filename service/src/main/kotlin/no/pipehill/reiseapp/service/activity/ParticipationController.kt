package no.pipehill.reiseapp.service.activity

import no.pipehill.reiseapp.api.ParticipationApi
import no.pipehill.reiseapp.api.dto.EnrollInActivityRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class ParticipationController(private val service: ParticipationService) : ParticipationApi {
    override fun enrollInActivity(personId: Long, enrollInActivityRequest: EnrollInActivityRequest): ResponseEntity<Unit> =
        service.enrollInActivity(personId, enrollInActivityRequest.activityId).toResponse()

    override fun cancelActivityEnrollment(personId: Long): ResponseEntity<Unit> =
        service.cancelActivityEnrollment(personId).toResponse()

    private fun ActivityEnrollmentResult.toResponse(): ResponseEntity<Unit> = when (this) {
        ActivityEnrollmentResult.SUCCESS -> ResponseEntity.noContent().build()
        ActivityEnrollmentResult.PERSON_NOT_FOUND, ActivityEnrollmentResult.ACTIVITY_NOT_FOUND ->
            ResponseEntity.notFound().build()
        ActivityEnrollmentResult.ACTIVITY_FULL -> ResponseEntity.status(409).build()
    }
}
