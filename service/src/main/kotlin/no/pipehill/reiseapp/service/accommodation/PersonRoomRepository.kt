package no.pipehill.reiseapp.service.accommodation

import no.pipehill.reiseapp.service.person.Person
import no.pipehill.reiseapp.service.room.Room
import org.springframework.data.repository.Repository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.repository.query.Param

interface PersonRoomRepository : Repository<PersonRoom, Long> {
    fun save(assignment: PersonRoom): PersonRoom

    @Query("SELECT COUNT(assignment) FROM PersonRoom assignment WHERE assignment.roomNumber = :roomNumber")
    fun countPersonsByRoomNumber(@Param("roomNumber") roomNumber: Int): Long

    @Modifying
    @Query("DELETE FROM PersonRoom assignment WHERE assignment.personId = :personId")
    fun deleteByPersonId(@Param("personId") personId: Long): Int

    @Query("SELECT room FROM Room room JOIN PersonRoom assignment ON assignment.roomNumber = room.roomNumber WHERE assignment.personId = :personId")
    fun findRoomByPersonId(@Param("personId") personId: Long): Room?

    @Query("SELECT person FROM Person person JOIN PersonRoom assignment ON assignment.personId = person.id WHERE assignment.roomNumber = :roomNumber ORDER BY person.id")
    fun findPersonsByRoomNumber(@Param("roomNumber") roomNumber: Int): List<Person>
}
