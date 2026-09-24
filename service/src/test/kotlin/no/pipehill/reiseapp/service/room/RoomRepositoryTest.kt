package no.pipehill.reiseapp.service.room

import java.time.LocalDate
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
class RoomRepositoryTest {
    @Autowired
    private lateinit var repository: RoomRepository

    @Autowired
    private lateinit var jdbc: JdbcTemplate

    @Test
    fun `finds one or all seeded rooms`() {
        val room = repository.findRoomByNumber(204)

        assertThat(room).isNotNull
        assertThat(room?.size).isEqualTo(23)
        assertThat(room?.numberOfBeds).isEqualTo(2)
        assertThat(room?.hasBalcony).isTrue()
        assertThat(room?.lastRenovatedYear).isEqualTo(LocalDate.of(2024, 1, 1))
        assertThat(repository.findRoomByNumber(Int.MAX_VALUE)).isNull()

        val rooms = repository.findAll()
        assertThat(rooms).hasSize(20)
        assertThat(rooms.map(Room::roomNumber)).isSorted.contains(101, 110, 201, 210)
        assertThat(rooms.map(Room::numberOfBeds)).contains(1, 2, 4)
        rooms.forEach {
            assertThat(it.lastRenovatedYear.monthValue).isEqualTo(1)
            assertThat(it.lastRenovatedYear.dayOfMonth).isEqualTo(1)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["2024-01-02", "2024-02-01", "1899-01-01", "2101-01-01"])
    fun `rejects renovation dates outside January first or supported years`(date: String) {
        assertThatThrownBy {
            jdbc.update("UPDATE room SET last_renovated_year = ? WHERE room_number = 204", LocalDate.parse(date))
        }.isInstanceOf(DataIntegrityViolationException::class.java)
    }
}
