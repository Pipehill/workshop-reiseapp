package no.pipehill.reiseapp.service.activity

import no.pipehill.reiseapp.service.person.PersonService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.Transactional

@EnabledIfEnvironmentVariable(named = "REISEAPP_TEST_DATABASE_URL", matches = ".+")
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = [
        "spring.datasource.url=\${REISEAPP_TEST_DATABASE_URL}",
        "spring.datasource.username=\${REISEAPP_TEST_DATABASE_USER}",
        "spring.datasource.password=\${REISEAPP_TEST_DATABASE_PASSWORD}",
    ],
)
@Transactional
class PersonActivityRepositoryTest {
    @Autowired
    private lateinit var repository: PersonActivityRepository
    @Autowired
    private lateinit var activities: ActivityService
    @Autowired
    private lateinit var persons: PersonService
    @Autowired
    private lateinit var jdbc: JdbcTemplate

    @Test
    fun `reads participation in both directions including empty relationships`() {
        assertThat(jdbc.queryForObject("SELECT count(*) FROM person_activity", Long::class.java)).isEqualTo(8)
        assertThat(repository.findParticipantsByActivityId(1).map { it.id }).containsExactly(1L, 2L)
        for (personId in 1L..8L) {
            val activity = repository.findActivityByPersonId(personId)
            assertThat(activity).isNotNull()
            assertThat(repository.findParticipantsByActivityId(requireNotNull(activity?.id)).map { it.id })
                .contains(personId)
        }
        assertThat(persons.findById(1)?.activity?.id).isEqualTo(1)
        assertThat(persons.findById(9)?.activity).isNull()
        assertThat(activities.findById(1)?.participants?.map { it.id }).containsExactly(1L, 2L)
        assertThat(activities.findById(5)?.participants).isEmpty()
        assertThat(repository.findActivityByPersonId(Long.MAX_VALUE)).isNull()
        assertThat(repository.findParticipantsByActivityId(Long.MAX_VALUE)).isEmpty()
        assertThat(jdbc.queryForList(
            "SELECT activity.id FROM activity JOIN person_activity ON activity.id = activity_id " +
                "GROUP BY activity.id HAVING count(*) > activity.max_participants",
        )).isEmpty()
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
