package no.pipehill.reiseapp.service.room

import java.time.LocalDate
import no.pipehill.reiseapp.api.dto.RoomResponse
import no.pipehill.reiseapp.service.accommodation.PersonRoomRepository
import no.pipehill.reiseapp.service.person.Person
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class RoomServiceTest {
    @Mock
    private lateinit var repository: RoomRepository

    @Mock
    private lateinit var assignments: PersonRoomRepository

    private lateinit var service: RoomService

    @BeforeEach
    fun setUp() {
        service = RoomService(repository, assignments)
    }

    @Test
    fun `maps one room or returns null`() {
        Mockito.`when`(repository.findRoomByNumber(204)).thenReturn(room())
        Mockito.`when`(repository.findRoomByNumber(999)).thenReturn(null)

        val result = service.findByNumber(204)
        assertThat(result?.roomNumber).isEqualTo(204)
        assertThat(result?.persons).isEmpty()
        assertThat(service.findByNumber(999)).isNull()
        Mockito.verify(assignments, Mockito.never()).findPersonsByRoomNumber(999)
    }

    @Test
    fun `maps all rooms`() {
        Mockito.`when`(repository.findAll()).thenReturn(
            listOf(room(), room(roomNumber = 205)),
        )

        assertThat(service.findAll()).containsExactly(
            roomResponse(),
            roomResponse(roomNumber = 205),
        )
    }

    @Test
    fun `loads assigned people only for room details`() {
        val person = Person("Test Person", "Test", "test@reiseapp.test", "+47 0000 0011", "mann", id = 11)
        Mockito.`when`(repository.findRoomByNumber(204)).thenReturn(room())
        Mockito.`when`(assignments.findPersonsByRoomNumber(204)).thenReturn(listOf(person))

        val result = service.findByNumber(204)

        assertThat(result?.persons).hasSize(1)
        assertThat(result?.persons?.first()).usingRecursiveComparison().isEqualTo(person)
        Mockito.clearInvocations(assignments)
        service.findAll()
        Mockito.verifyNoInteractions(assignments)
    }

    private fun room(roomNumber: Int = 204): Room =
        Room(
            roomNumber = roomNumber,
            sizeSquareMeters = 23,
            numberOfBeds = 2,
            hasBalcony = true,
            lastRenovatedYear = LocalDate.of(2024, 1, 1),
        )

    private fun roomResponse(roomNumber: Int = 204): RoomResponse =
        RoomResponse(
            roomNumber = roomNumber,
            sizeSquareMeters = 23,
            numberOfBeds = RoomResponse.NumberOfBeds._2,
            hasBalcony = true,
            lastRenovatedYear = LocalDate.of(2024, 1, 1),
        )
}
