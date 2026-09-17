package no.pipehill.reiseapp.service.accommodation

import no.pipehill.reiseapp.service.person.PersonRepository
import no.pipehill.reiseapp.service.room.RoomRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

enum class RoomAssignmentResult {
    SUCCESS,
    PERSON_NOT_FOUND,
    ROOM_NOT_FOUND,
    ROOM_FULL,
}

@Service
class AccommodationService(
    private val persons: PersonRepository,
    private val rooms: RoomRepository,
    private val assignments: PersonRoomRepository,
) {
    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun assignRoom(personId: Long, roomNumber: Int): RoomAssignmentResult {
        // Serialize changes for the same person, including removal.
        persons.findPersonByIdForUpdate(personId) ?: return RoomAssignmentResult.PERSON_NOT_FOUND
        val currentRoom = assignments.findRoomByPersonId(personId)

        // Lock both rooms in a fixed order to avoid deadlocks during opposite moves.
        val lockedRooms = listOfNotNull(currentRoom?.roomNumber, roomNumber)
            .distinct().sorted().associateWith { rooms.findRoomByNumberForUpdate(it) }
        val targetRoom = lockedRooms[roomNumber] ?: return RoomAssignmentResult.ROOM_NOT_FOUND
        if (currentRoom?.roomNumber == roomNumber) return RoomAssignmentResult.SUCCESS

        // The room lock is held until commit, including the capacity check and save.
        if (assignments.countPersonsByRoomNumber(roomNumber) >= targetRoom.numberOfBeds) {
            return RoomAssignmentResult.ROOM_FULL
        }
        assignments.save(PersonRoom(personId, roomNumber))
        return RoomAssignmentResult.SUCCESS
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun removeRoomAssignment(personId: Long): RoomAssignmentResult {
        persons.findPersonByIdForUpdate(personId) ?: return RoomAssignmentResult.PERSON_NOT_FOUND
        val currentRoom = assignments.findRoomByPersonId(personId)
            ?: return RoomAssignmentResult.SUCCESS
        rooms.findRoomByNumberForUpdate(currentRoom.roomNumber)
        assignments.deleteByPersonId(personId)
        return RoomAssignmentResult.SUCCESS
    }
}
