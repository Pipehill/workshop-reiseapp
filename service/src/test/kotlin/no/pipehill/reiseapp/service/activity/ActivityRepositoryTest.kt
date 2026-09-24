package no.pipehill.reiseapp.service.activity

import java.time.Duration
import java.time.LocalDateTime
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
class ActivityRepositoryTest {
    @Autowired
    private lateinit var repository: ActivityRepository
    @Autowired
    private lateinit var jdbc: JdbcTemplate

    @Test
    fun `reads five seeded activities with notes`() {
        val activities = repository.findAll()
        assertThat(activities).hasSize(5)
        assertThat(activities.map { it.id }).containsExactly(5L, 1L, 2L, 4L, 3L)
        activities.forEach {
            assertThat(Duration.between(it.startTime, it.endTime).toHours()).isBetween(2L, 8L)
            assertThat(it.maxParticipants).isPositive()
            assertThat(it.notes).isNotBlank()
        }
        val activity = repository.findActivityById(1)
        assertThat(activity?.startTime).isEqualTo(LocalDateTime.parse("2026-10-13T09:00:00"))
        assertThat(activity?.endTime).isEqualTo(LocalDateTime.parse("2026-10-13T15:00:00"))
        assertThat(activity?.notes).isEqualTo(
            "Bruk gode tursko med godt grep.\n" +
                "Ta med varme klær og vind- og regntett jakke.\n" +
                "Pakk matpakke og vann i en komfortabel sekk.",
        )
        assertThat(repository.findActivityById(Long.MAX_VALUE)).isNull()
    }

    @ParameterizedTest
    @ValueSource(ints = [119, 481, 0, -60])
    fun `database rejects durations outside two to eight hours`(minutes: Int) {
        assertThatThrownBy {
            jdbc.update("UPDATE activity SET end_time = start_time + (? * INTERVAL '1 minute') WHERE id = 1", minutes)
        }.isInstanceOf(DataIntegrityViolationException::class.java)
    }

    @Test
    fun `database rejects activities crossing midnight`() {
        assertThatThrownBy {
            jdbc.update("UPDATE activity SET start_time = '2026-10-13 23:00:00', " +
                "end_time = '2026-10-14 02:00:00' WHERE id = 1")
        }.isInstanceOf(DataIntegrityViolationException::class.java)
    }

    @Test
    fun `sorts activities by date before time of day`() {
        jdbc.update("UPDATE activity SET start_time = '2026-10-14 07:00:00', " +
            "end_time = '2026-10-14 09:00:00' WHERE id = 5")

        assertThat(repository.findAll().map { it.id }).containsExactly(1L, 2L, 4L, 3L, 5L)
    }

    @Test
    fun `database rejects zero capacity`() {
        assertThatThrownBy {
            jdbc.update("UPDATE activity SET max_participants = 0 WHERE id = 1")
        }.isInstanceOf(DataIntegrityViolationException::class.java)
    }
}
