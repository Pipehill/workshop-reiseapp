package no.pipehill.reiseapp.service.activity

import no.pipehill.reiseapp.api.ActivityApi
import no.pipehill.reiseapp.api.dto.ActivityResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class ActivityController(private val service: ActivityService) : ActivityApi {
    override fun listActivities(): ResponseEntity<List<ActivityResponse>> =
        ResponseEntity.ok(service.findAll())

    override fun getActivity(activityId: Long): ResponseEntity<ActivityResponse> =
        service.findById(activityId)?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
}
