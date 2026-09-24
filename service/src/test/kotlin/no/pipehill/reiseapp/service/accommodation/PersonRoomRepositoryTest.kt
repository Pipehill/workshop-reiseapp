package no.pipehill.reiseapp.service.accommodation

import no.pipehill.reiseapp.service.support.DatabaseTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate

@DatabaseTest
class PersonRoomRepositoryTest {
    @Autowired
    private lateinit var repository: PersonRoomRepository

    @Autowired
    private lateinit var jdbc: JdbcTemplate

    @Test
    fun `seeded assignments are consistent in both directions and fit room capacity`() {
        assertThat(jdbc.queryForObject("SELECT count(*) FROM person_room", Long::class.java)).isEqualTo(20)
        for (personId in 11L..20L) {
            assertThat(repository.findRoomByPersonId(personId)).`as`("room for seeded person %s", personId).isNotNull()
        }
        val persons = repository.findPersonsByRoomNumber(108)
        assertThat(persons.map { it.id }).containsExactly(5L, 6L, 7L, 8L)
        persons.forEach {
            assertThat(repository.findRoomByPersonId(requireNotNull(it.id))?.roomNumber)
                .`as`("reverse lookup for person %s", it.id).isEqualTo(108)
        }
        assertThat(repository.findPersonsByRoomNumber(210)).isEmpty()
        assertThat(repository.findRoomByPersonId(Long.MAX_VALUE)).isNull()
        assertThat(jdbc.queryForList(
            "SELECT room.room_number FROM room JOIN person_room USING (room_number) " +
                "GROUP BY room.room_number, room.number_of_beds HAVING count(*) > room.number_of_beds",
        )).isEmpty()
    }
}
