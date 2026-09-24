package no.pipehill.reiseapp.service.room

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "room")
class Room(
    @field:Id
    @field:Column(name = "room_number", nullable = false, updatable = false)
    var roomNumber: Int,
    @field:Column(name = "size", nullable = false)
    var size: Int,
    @field:Column(name = "number_of_beds", nullable = false)
    var numberOfBeds: Int,
    @field:Column(name = "has_balcony", nullable = false)
    var hasBalcony: Boolean,
    @field:Column(name = "last_renovated_year", nullable = false)
    var lastRenovatedYear: LocalDate,
)
