package no.pipehill.reiseapp.service.accommodation

import no.pipehill.reiseapp.service.person.Person
import no.pipehill.reiseapp.service.room.Room
import org.springframework.data.repository.Repository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PersonRoomRepository : Repository<PersonRoom, Long> {
    @Query("SELECT room FROM Room room JOIN PersonRoom assignment ON assignment.roomNumber = room.roomNumber WHERE assignment.personId = :personId")
    fun findRoomByPersonId(@Param("personId") personId: Long): Room?

    @Query("SELECT person FROM Person person JOIN PersonRoom assignment ON assignment.personId = person.id WHERE assignment.roomNumber = :roomNumber ORDER BY person.id")
    fun findPersonsByRoomNumber(@Param("roomNumber") roomNumber: Int): List<Person>
}
