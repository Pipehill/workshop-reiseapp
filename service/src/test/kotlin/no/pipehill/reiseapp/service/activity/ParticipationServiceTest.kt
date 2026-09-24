package no.pipehill.reiseapp.service.activity

import no.pipehill.reiseapp.service.support.concurrently
import no.pipehill.reiseapp.service.support.DatabaseTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@DatabaseTest
class ParticipationServiceTest {
    @Autowired
    private lateinit var service: ParticipationService
    @Autowired
    private lateinit var participation: PersonActivityRepository
    @Autowired
    private lateinit var jdbc: JdbcTemplate

    @Test
    fun `switches activity then cancels enrollment`() {
        assertThat(service.enrollInActivity(1, 2)).isEqualTo(ActivityEnrollmentResult.SUCCESS)
        assertThat(participation.countParticipantsByActivityId(2)).isEqualTo(5)
        assertThat(participation.findActivityByPersonId(1)?.id).isEqualTo(2)
        assertThat(participation.findParticipantsByActivityId(2).map { it.id }).contains(1L)
        assertThat(participation.findParticipantsByActivityId(1).map { it.id }).doesNotContain(1L)
        assertThat(service.cancelActivityEnrollment(1)).isEqualTo(ActivityEnrollmentResult.SUCCESS)
        assertThat(service.cancelActivityEnrollment(1)).isEqualTo(ActivityEnrollmentResult.SUCCESS)
        assertThat(participation.findActivityByPersonId(1)).isNull()
        assertThat(participation.findParticipantsByActivityId(2).map { it.id }).doesNotContain(1L)
    }

    @Test
    fun `full and missing activities leave old enrollment intact`() {
        jdbc.update("UPDATE activity SET max_participants = 4 WHERE id = 2")
        assertThat(service.enrollInActivity(1, 2)).isEqualTo(ActivityEnrollmentResult.ACTIVITY_FULL)
        assertThat(service.enrollInActivity(1, Long.MAX_VALUE)).isEqualTo(ActivityEnrollmentResult.ACTIVITY_NOT_FOUND)
        assertThat(participation.findActivityByPersonId(1)?.id).isEqualTo(1)
        assertThat(service.enrollInActivity(Long.MAX_VALUE, 1)).isEqualTo(ActivityEnrollmentResult.PERSON_NOT_FOUND)
        assertThat(service.cancelActivityEnrollment(Long.MAX_VALUE)).isEqualTo(ActivityEnrollmentResult.PERSON_NOT_FOUND)
    }

    @Test
    fun `same full activity is idempotent and cancellation frees capacity`() {
        jdbc.update("UPDATE activity SET max_participants = 4 WHERE id = 1")
        assertThat(service.enrollInActivity(1, 1)).isEqualTo(ActivityEnrollmentResult.SUCCESS)
        assertThat(participation.countParticipantsByActivityId(1)).isEqualTo(4)
        assertThat(service.cancelActivityEnrollment(1)).isEqualTo(ActivityEnrollmentResult.SUCCESS)
        assertThat(service.enrollInActivity(9, 1)).isEqualTo(ActivityEnrollmentResult.SUCCESS)
        assertThat(participation.countParticipantsByActivityId(1)).isEqualTo(4)
        assertThat(service.enrollInActivity(1, 1)).isEqualTo(ActivityEnrollmentResult.ACTIVITY_FULL)
        assertThat(participation.findActivityByPersonId(1)).isNull()
        assertThat(service.enrollInActivity(1, 2)).isEqualTo(ActivityEnrollmentResult.SUCCESS)
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun `concurrent enrollments cannot take the same last place`() {
        withFixtures { first, second, activityA, _ ->
            assertThat(concurrently(
                { service.enrollInActivity(first, activityA) },
                { service.enrollInActivity(second, activityA) },
            )).containsExactlyInAnyOrder(ActivityEnrollmentResult.SUCCESS, ActivityEnrollmentResult.ACTIVITY_FULL)
            assertThat(participation.countParticipantsByActivityId(activityA)).isEqualTo(1)
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun `concurrent enrollments for same person keep a single activity`() {
        withFixtures { first, _, activityA, activityB ->
            assertThat(concurrently(
                { service.enrollInActivity(first, activityA) },
                { service.enrollInActivity(first, activityB) },
            )).containsOnly(ActivityEnrollmentResult.SUCCESS)
            assertThat(participation.countParticipantsByActivityId(activityA) +
                participation.countParticipantsByActivityId(activityB)).isEqualTo(1)
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun `opposite activity changes complete without deadlock`() {
        withFixtures { first, second, activityA, activityB ->
            jdbc.update("UPDATE activity SET max_participants = 2 WHERE id IN (?, ?)", activityA, activityB)
            service.enrollInActivity(first, activityA)
            service.enrollInActivity(second, activityB)
            assertThat(concurrently(
                { service.enrollInActivity(first, activityB) },
                { service.enrollInActivity(second, activityA) },
            )).containsOnly(ActivityEnrollmentResult.SUCCESS)
            assertThat(participation.findActivityByPersonId(first)?.id).isEqualTo(activityB)
            assertThat(participation.findActivityByPersonId(second)?.id).isEqualTo(activityA)
        }
    }

    private fun withFixtures(test: (Long, Long, Long, Long) -> Unit) {
        val personIds = mutableListOf<Long>()
        val activityIds = mutableListOf<Long>()
        try {
            for (number in 1..2) {
                activityIds.add(checkNotNull(jdbc.queryForObject(
                    "INSERT INTO activity (title, description, max_participants, start_time, end_time) " +
                        "VALUES ('Test', 'Test', 1, '2026-10-13 09:00:00', '2026-10-13 11:00:00') RETURNING id",
                    Long::class.java,
                )))
                personIds.add(checkNotNull(jdbc.queryForObject(
                    "INSERT INTO person (name, department, email, phone_number, gender) " +
                        "VALUES ('Test', 'Test', ?, '0000', 0) RETURNING id",
                    Long::class.java, "activity-concurrency-$number@reiseapp.test",
                )))
            }
            test(personIds[0], personIds[1], activityIds[0], activityIds[1])
        } finally {
            personIds.forEach { jdbc.update("DELETE FROM person WHERE id = ?", it) }
            activityIds.forEach { jdbc.update("DELETE FROM activity WHERE id = ?", it) }
        }
    }
}
