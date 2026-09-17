package no.pipehill.reiseapp.service.activity

import no.pipehill.reiseapp.api.dto.ActivityResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ActivityService(private val repository: ActivityRepository) {
    @Transactional(readOnly = true)
    fun findAll(): List<ActivityResponse> = repository.findAll().map { it.toResponse() }

    @Transactional(readOnly = true)
    fun findById(id: Long): ActivityResponse? = repository.findActivityById(id)?.toResponse()

    private fun Activity.toResponse() = ActivityResponse(
        id = checkNotNull(id) { "A persisted activity must have an id" },
        title = title,
        description = description,
        maxParticipants = maxParticipants,
        startTime = startTime,
        endTime = endTime,
        notes = notes,
    )
}
