package no.pipehill.reiseapp.service.room

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
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

    @Test
    fun `finds one or all seeded rooms`() {
        val room = repository.findRoomByNumber(204)

        assertThat(room).isNotNull
        assertThat(room?.sizeSquareMeters).isEqualTo(23)
        assertThat(room?.numberOfBeds).isEqualTo(2)
        assertThat(room?.hasBalcony).isTrue()
        assertThat(room?.lastRenovatedYear).isEqualTo(2024)
        assertThat(repository.findRoomByNumber(Int.MAX_VALUE)).isNull()

        val rooms = repository.findAll()
        assertThat(rooms).hasSize(20)
        assertThat(rooms.map(Room::roomNumber)).isSorted.contains(101, 110, 201, 210)
        assertThat(rooms.map(Room::numberOfBeds)).contains(1, 2, 4)
    }
}
