package no.pipehill.reiseapp.service.activity

import no.pipehill.reiseapp.api.dto.ActivityResponse
import no.pipehill.reiseapp.api.dto.ActivityDetailsResponse
import no.pipehill.reiseapp.api.dto.PersonResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ActivityService(
    private val repository: ActivityRepository,
    private val participation: PersonActivityRepository,
) {
    @Transactional(readOnly = true)
    fun findAll(): List<ActivityResponse> = repository.findAll().map { it.toResponse() }

    @Transactional(readOnly = true)
    fun findById(id: Long): ActivityDetailsResponse? {
        val activity = repository.findActivityById(id) ?: return null
        return ActivityDetailsResponse(
            id = checkNotNull(activity.id),
            title = activity.title,
            description = activity.description,
            maxParticipants = activity.maxParticipants,
            startTime = activity.startTime.toActivityTimeString(),
            endTime = activity.endTime.toActivityTimeString(),
            notes = activity.notes,
            participants = participation.findParticipantsByActivityId(id).map {
                PersonResponse(
                    id = checkNotNull(it.id),
                    name = it.name,
                    department = it.department,
                    email = it.email,
                    phoneNumber = it.phoneNumber,
                    gender = it.gender,
                    registrationDate = it.registrationDate,
                )
            },
        )
    }

    private fun Activity.toResponse() = ActivityResponse(
        id = checkNotNull(id) { "A persisted activity must have an id" },
        title = title,
        description = description,
        maxParticipants = maxParticipants,
        startTime = startTime.toActivityTimeString(),
        endTime = endTime.toActivityTimeString(),
        notes = notes,
    )
}
