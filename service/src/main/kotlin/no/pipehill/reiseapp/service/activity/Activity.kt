package no.pipehill.reiseapp.service.activity

import jakarta.persistence.*
import java.time.LocalTime

@Entity
@Table(name = "activity")
class Activity(
    @field:Column(nullable = false, length = 200)
    var title: String,
    @field:Column(nullable = false, columnDefinition = "text")
    var description: String,
    @field:Column(name = "max_participants", nullable = false)
    var maxParticipants: Int,
    @field:Column(name = "start_time", nullable = false)
    var startTime: LocalTime,
    @field:Column(name = "end_time", nullable = false)
    var endTime: LocalTime,
    @field:Column(nullable = false, columnDefinition = "text")
    var notes: String = "",
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
)
