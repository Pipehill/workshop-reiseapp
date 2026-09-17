package no.pipehill.reiseapp.service.accommodation

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "person_room")
class PersonRoom(
    @field:Id
    @field:Column(name = "person_id", nullable = false)
    var personId: Long,
    @field:Column(name = "room_number", nullable = false)
    var roomNumber: Int,
)
