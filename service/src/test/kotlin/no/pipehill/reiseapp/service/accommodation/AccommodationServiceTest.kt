package no.pipehill.reiseapp.service.accommodation

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import no.pipehill.reiseapp.service.person.PersonService
import no.pipehill.reiseapp.service.room.RoomService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.Propagation
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
class AccommodationServiceTest {
    @Autowired
    private lateinit var service: AccommodationService
    @Autowired
    private lateinit var assignments: PersonRoomRepository
    @Autowired
    private lateinit var persons: PersonService
    @Autowired
    private lateinit var rooms: RoomService
    @Autowired
    private lateinit var jdbc: JdbcTemplate

    @Test
    fun `moves person and updates detail views then removes assignment`() {
        assertThat(service.assignRoom(1, 106)).isEqualTo(RoomAssignmentResult.SUCCESS)
        // Flush the managed assignment before bulk deletion later in this test.
        assertThat(assignments.countPersonsByRoomNumber(106)).isEqualTo(1)
        assertThat(persons.findById(1)?.assignedRoom?.roomNumber).isEqualTo(106)
        assertThat(rooms.findByNumber(106)?.persons?.map { it.id }).contains(1L)
        assertThat(rooms.findByNumber(101)?.persons).isEmpty()
        assertThat(service.removeRoomAssignment(1)).isEqualTo(RoomAssignmentResult.SUCCESS)
        assertThat(service.removeRoomAssignment(1)).isEqualTo(RoomAssignmentResult.SUCCESS)
        assertThat(persons.findById(1)?.assignedRoom).isNull()
        assertThat(rooms.findByNumber(106)?.persons).isEmpty()
    }

    @Test
    fun `full and missing rooms leave existing assignment intact`() {
        assertThat(service.assignRoom(1, 104)).isEqualTo(RoomAssignmentResult.ROOM_FULL)
        assertThat(service.assignRoom(1, 99999)).isEqualTo(RoomAssignmentResult.ROOM_NOT_FOUND)
        assertThat(persons.findById(1)?.assignedRoom?.roomNumber).isEqualTo(101)
        assertThat(service.assignRoom(Long.MAX_VALUE, 106)).isEqualTo(RoomAssignmentResult.PERSON_NOT_FOUND)
        assertThat(service.removeRoomAssignment(Long.MAX_VALUE)).isEqualTo(RoomAssignmentResult.PERSON_NOT_FOUND)
    }

    @Test
    fun `same full room is idempotent and removing frees a bed`() {
        assertThat(service.assignRoom(1, 101)).isEqualTo(RoomAssignmentResult.SUCCESS)
        assertThat(assignments.countPersonsByRoomNumber(101)).isEqualTo(1)
        assertThat(service.removeRoomAssignment(1)).isEqualTo(RoomAssignmentResult.SUCCESS)
        assertThat(service.assignRoom(2, 101)).isEqualTo(RoomAssignmentResult.SUCCESS)
        assertThat(assignments.countPersonsByRoomNumber(101)).isEqualTo(1)
        assertThat(service.assignRoom(1, 101)).isEqualTo(RoomAssignmentResult.ROOM_FULL)
        assertThat(persons.findById(1)?.assignedRoom).isNull()
        assertThat(service.assignRoom(1, 106)).isEqualTo(RoomAssignmentResult.SUCCESS)
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun `concurrent assignments cannot take the same last bed`() {
        withConcurrentFixtures { first, second ->
            val results = concurrently(
                { service.assignRoom(first, 9901) },
                { service.assignRoom(second, 9901) },
            )
            assertThat(results).containsExactlyInAnyOrder(RoomAssignmentResult.SUCCESS, RoomAssignmentResult.ROOM_FULL)
            assertThat(assignments.countPersonsByRoomNumber(9901)).isEqualTo(1)
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun `concurrent assignments for same person keep a single room`() {
        withConcurrentFixtures { first, _ ->
            val results = concurrently(
                { service.assignRoom(first, 9901) },
                { service.assignRoom(first, 9902) },
            )
            assertThat(results).containsOnly(RoomAssignmentResult.SUCCESS)
            assertThat(assignments.countPersonsByRoomNumber(9901) +
                assignments.countPersonsByRoomNumber(9902)).isEqualTo(1)
        }
    }

    private fun concurrently(
        first: () -> RoomAssignmentResult,
        second: () -> RoomAssignmentResult,
    ): List<RoomAssignmentResult> {
        val ready = CountDownLatch(2)
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(2)
        try {
            val futures = listOf(first, second).map { action ->
                executor.submit<RoomAssignmentResult> {
                    ready.countDown()
                    check(start.await(10, TimeUnit.SECONDS))
                    action()
                }
            }
            check(ready.await(10, TimeUnit.SECONDS))
            start.countDown()
            return futures.map { it.get(15, TimeUnit.SECONDS) }
        } finally {
            start.countDown()
            executor.shutdownNow()
            executor.awaitTermination(10, TimeUnit.SECONDS)
        }
    }

    private fun withConcurrentFixtures(test: (Long, Long) -> Unit) {
        val personIds = mutableListOf<Long>()
        val roomNumbers = mutableListOf<Int>()
        try {
            for (number in listOf(9901, 9902)) {
                jdbc.update("INSERT INTO room VALUES (?, 20, 1, FALSE, DATE '2024-01-01')", number)
                roomNumbers.add(number)
                personIds.add(checkNotNull(jdbc.queryForObject(
                    "INSERT INTO person (name, department, email, phone_number, gender) " +
                        "VALUES ('Test', 'Test', ?, '0000', 'ukjent') RETURNING id",
                    Long::class.java, "room-concurrency-$number@reiseapp.test",
                )))
            }
            test(personIds[0], personIds[1])
        } finally {
            personIds.forEach { jdbc.update("DELETE FROM person WHERE id = ?", it) }
            roomNumbers.forEach { jdbc.update("DELETE FROM room WHERE room_number = ?", it) }
        }
    }
}
