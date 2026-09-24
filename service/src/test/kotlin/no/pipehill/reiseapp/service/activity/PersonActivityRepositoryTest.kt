package no.pipehill.reiseapp.service.activity

import no.pipehill.reiseapp.service.support.DatabaseTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.JdbcTemplate

@DatabaseTest
class PersonActivityRepositoryTest {
    @Autowired
    private lateinit var repository: PersonActivityRepository
    @Autowired
    private lateinit var jdbc: JdbcTemplate

    @Test
    fun `reads participation in both directions including empty relationships`() {
        assertThat(jdbc.queryForObject("SELECT count(*) FROM person_activity", Long::class.java)).isEqualTo(18)
        assertThat(repository.findParticipantsByActivityId(1).map { it.id }).containsExactly(1L, 2L, 11L, 12L)
        for (personId in (1L..8L) + (11L..20L)) {
            val activity = repository.findActivityByPersonId(personId)
            assertThat(activity).`as`("activity for seeded person %s", personId).isNotNull()
            assertThat(repository.findParticipantsByActivityId(requireNotNull(activity?.id)).map { it.id })
                .`as`("reverse lookup for person %s", personId).contains(personId)
        }
        assertThat(repository.findActivityByPersonId(9)).isNull()
        assertThat(repository.findParticipantsByActivityId(5).map { it.id }).containsExactly(19L, 20L)
        assertThat(repository.findActivityByPersonId(Long.MAX_VALUE)).isNull()
        assertThat(repository.findParticipantsByActivityId(Long.MAX_VALUE)).isEmpty()
        assertThat(jdbc.queryForList(
            "SELECT activity.id FROM activity JOIN person_activity ON activity.id = activity_id " +
                "GROUP BY activity.id HAVING count(*) > activity.max_participants",
        )).isEmpty()
    }

    @Test
    fun `activity without participants returns an empty list`() {
        jdbc.update("DELETE FROM person_activity WHERE activity_id = 5")
        assertThat(repository.findParticipantsByActivityId(5)).isEmpty()
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "INSERT INTO person_activity VALUES (1, 2)",
        "INSERT INTO person_activity VALUES (999999, 1)",
        "INSERT INTO person_activity VALUES (9, 999999)",
    ])
    fun `database rejects second activity and missing references`(sql: String) {
        assertThatThrownBy { jdbc.update(sql) }.isInstanceOf(DataIntegrityViolationException::class.java)
    }
}
