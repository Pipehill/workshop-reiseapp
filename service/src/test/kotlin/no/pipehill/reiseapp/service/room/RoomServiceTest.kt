package no.pipehill.reiseapp.service.room

import no.pipehill.reiseapp.api.dto.RoomResponse
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

    private lateinit var service: RoomService

    @BeforeEach
    fun setUp() {
        service = RoomService(repository)
    }

    @Test
    fun `maps one room or returns null`() {
        Mockito.`when`(repository.findRoomByNumber(204)).thenReturn(room())
        Mockito.`when`(repository.findRoomByNumber(999)).thenReturn(null)

        assertThat(service.findByNumber(204)).isEqualTo(roomResponse())
        assertThat(service.findByNumber(999)).isNull()
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

    private fun room(roomNumber: Int = 204): Room =
        Room(
            roomNumber = roomNumber,
            sizeSquareMeters = 23,
            numberOfBeds = 2,
            hasBalcony = true,
            lastRenovatedYear = 2024,
        )

    private fun roomResponse(roomNumber: Int = 204): RoomResponse =
        RoomResponse(
            roomNumber = roomNumber,
            sizeSquareMeters = 23,
            numberOfBeds = RoomResponse.NumberOfBeds._2,
            hasBalcony = true,
            lastRenovatedYear = 2024,
        )
}
