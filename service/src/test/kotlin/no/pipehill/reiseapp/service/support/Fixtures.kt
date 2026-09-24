package no.pipehill.reiseapp.service.support

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import no.pipehill.reiseapp.service.activity.Activity
import no.pipehill.reiseapp.service.person.Person
import no.pipehill.reiseapp.service.room.Room

object Fixtures {
    fun person(id: Long = 11, gender: Int = 1) = Person(
        name = "Test Person", department = "Test", email = "test@reiseapp.test",
        phoneNumber = "0000", gender = gender, id = id,
        registrationDate = OffsetDateTime.parse("2026-09-15T10:30:00+02:00"),
    )

    fun room() = Room(204, 23, 2, true, LocalDate.of(2024, 1, 1))

    fun activity() = Activity(
        title = "Fjelltur", description = "Tur med guide", maxParticipants = 16,
        startTime = LocalDateTime.parse("2026-10-13T09:00:00"),
        endTime = LocalDateTime.parse("2026-10-13T15:00:00"),
        notes = "<p><strong>Gode sko.</strong><br>Mat &amp; vann.</p>", id = 1,
    )
}
