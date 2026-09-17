package no.pipehill.reiseapp.service.activity

import no.pipehill.reiseapp.service.person.PersonRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

enum class ActivityEnrollmentResult {
    SUCCESS,
    PERSON_NOT_FOUND,
    ACTIVITY_NOT_FOUND,
    ACTIVITY_FULL,
}

@Service
class ParticipationService(
    private val persons: PersonRepository,
    private val activities: ActivityRepository,
    private val participation: PersonActivityRepository,
) {
    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun enrollInActivity(personId: Long, activityId: Long): ActivityEnrollmentResult {
        // Serialize all enrollment changes for a person, including cancellation.
        persons.findPersonByIdForUpdate(personId) ?: return ActivityEnrollmentResult.PERSON_NOT_FOUND
        val currentActivity = participation.findActivityByPersonId(personId)

        // Fixed lock order prevents deadlocks when people switch in opposite directions.
        val lockedActivities = listOfNotNull(currentActivity?.id, activityId)
            .distinct().sorted().associateWith { activities.findActivityByIdForUpdate(it) }
        val target = lockedActivities[activityId] ?: return ActivityEnrollmentResult.ACTIVITY_NOT_FOUND
        if (currentActivity?.id == activityId) return ActivityEnrollmentResult.SUCCESS

        // Hold the activity lock through the capacity check, save and commit.
        if (participation.countParticipantsByActivityId(activityId) >= target.maxParticipants) {
            return ActivityEnrollmentResult.ACTIVITY_FULL
        }
        participation.save(PersonActivity(personId, activityId))
        return ActivityEnrollmentResult.SUCCESS
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun cancelActivityEnrollment(personId: Long): ActivityEnrollmentResult {
        persons.findPersonByIdForUpdate(personId) ?: return ActivityEnrollmentResult.PERSON_NOT_FOUND
        val currentActivity = participation.findActivityByPersonId(personId)
            ?: return ActivityEnrollmentResult.SUCCESS
        activities.findActivityByIdForUpdate(checkNotNull(currentActivity.id))
        participation.deleteByPersonId(personId)
        return ActivityEnrollmentResult.SUCCESS
    }
}
