package no.pipehill.reiseapp.service.accommodation

import no.pipehill.reiseapp.api.AccommodationApi
import no.pipehill.reiseapp.api.dto.AssignRoomRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class AccommodationController(private val service: AccommodationService) : AccommodationApi {
    override fun assignRoom(personId: Long, assignRoomRequest: AssignRoomRequest): ResponseEntity<Unit> =
        service.assignRoom(personId, assignRoomRequest.roomNumber).toResponse()

    override fun removeRoomAssignment(personId: Long): ResponseEntity<Unit> =
        service.removeRoomAssignment(personId).toResponse()

    private fun RoomAssignmentResult.toResponse(): ResponseEntity<Unit> = when (this) {
        RoomAssignmentResult.SUCCESS -> ResponseEntity.noContent().build()
        RoomAssignmentResult.PERSON_NOT_FOUND, RoomAssignmentResult.ROOM_NOT_FOUND ->
            ResponseEntity.notFound().build()
        RoomAssignmentResult.ROOM_FULL -> ResponseEntity.status(409).build()
    }
}
