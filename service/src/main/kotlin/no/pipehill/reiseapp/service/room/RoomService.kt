package no.pipehill.reiseapp.service.room

import no.pipehill.reiseapp.api.dto.RoomResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RoomService(
    private val repository: RoomRepository,
) {
    @Transactional(readOnly = true)
    fun findByNumber(roomNumber: Int): RoomResponse? =
        repository.findRoomByNumber(roomNumber)?.toResponse()

    @Transactional(readOnly = true)
    fun findAll(): List<RoomResponse> = repository.findAll().map { it.toResponse() }

    private fun Room.toResponse(): RoomResponse =
        RoomResponse(
            roomNumber = roomNumber,
            sizeSquareMeters = sizeSquareMeters,
            numberOfBeds = RoomResponse.NumberOfBeds.forValue(numberOfBeds),
            hasBalcony = hasBalcony,
            lastRenovatedYear = lastRenovatedYear,
        )
}
