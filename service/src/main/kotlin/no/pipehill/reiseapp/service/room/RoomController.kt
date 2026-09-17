package no.pipehill.reiseapp.service.room

import no.pipehill.reiseapp.api.RoomApi
import no.pipehill.reiseapp.api.dto.RoomResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class RoomController(
    private val service: RoomService,
) : RoomApi {
    override fun getRoom(roomNumber: Int): ResponseEntity<RoomResponse> =
        service.findByNumber(roomNumber)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    override fun listRooms(): ResponseEntity<List<RoomResponse>> =
        ResponseEntity.ok(service.findAll())
}
