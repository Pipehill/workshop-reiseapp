package no.pipehill.reiseapp.service.room

import no.pipehill.reiseapp.api.dto.RoomResponse
import no.pipehill.reiseapp.api.dto.RoomDetailsResponse
import no.pipehill.reiseapp.api.dto.PersonResponse
import no.pipehill.reiseapp.service.accommodation.PersonRoomRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RoomService(
    private val repository: RoomRepository,
    private val assignments: PersonRoomRepository,
) {
    @Transactional(readOnly = true)
    fun findByNumber(roomNumber: Int): RoomDetailsResponse? {
        val room = repository.findRoomByNumber(roomNumber) ?: return null
        return RoomDetailsResponse(
            roomNumber = room.roomNumber,
            propertySize = room.size,
            numberOfBeds = RoomDetailsResponse.NumberOfBeds.forValue(room.numberOfBeds),
            hasBalcony = room.hasBalcony,
            lastRenovatedYear = room.lastRenovatedYear,
            persons = assignments.findPersonsByRoomNumber(roomNumber).map {
                PersonResponse(
                    id = checkNotNull(it.id),
                    name = it.name,
                    department = it.department,
                    email = it.email,
                    phoneNumber = it.phoneNumber,
                    gender = it.gender,
                    registrationDate = it.registrationDate,
                )
            },
        )
    }

    @Transactional(readOnly = true)
    fun findAll(): List<RoomResponse> = repository.findAll().map { it.toResponse() }

    private fun Room.toResponse(): RoomResponse =
        RoomResponse(
            roomNumber = roomNumber,
            propertySize = size,
            numberOfBeds = RoomResponse.NumberOfBeds.forValue(numberOfBeds),
            hasBalcony = hasBalcony,
            lastRenovatedYear = lastRenovatedYear,
        )
}
