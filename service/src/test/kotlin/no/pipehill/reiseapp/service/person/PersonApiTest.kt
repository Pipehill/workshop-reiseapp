package no.pipehill.reiseapp.service.person

import java.time.OffsetDateTime
import java.time.ZoneOffset
import no.pipehill.reiseapp.service.accommodation.PersonRoomRepository
import no.pipehill.reiseapp.service.activity.PersonActivityRepository
import no.pipehill.reiseapp.service.support.Fixtures
import no.pipehill.reiseapp.service.support.HttpApiTest
import no.pipehill.reiseapp.service.support.HttpTestSupport
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean

@HttpApiTest
@Import(PersonController::class, PersonService::class)
class PersonApiTest : HttpTestSupport() {
    @MockitoBean
    private lateinit var repository: PersonRepository
    @MockitoBean
    private lateinit var assignments: PersonRoomRepository
    @MockitoBean
    private lateinit var participation: PersonActivityRepository

    @Test
    fun `list exposes person fields without loading relationships`() {
        Mockito.`when`(repository.findAll()).thenReturn(listOf(Fixtures.person()))

        assertThat(request("/persons").json()).isEqualTo(parseJson("""[
            {"id":11,"name":"Test Person","department":"Test","email":"test@reiseapp.test",
             "phoneNumber":"0000","gender":1,"registrationDate":"2026-09-15T10:30:00+02:00"}
        ]"""))
        Mockito.verifyNoInteractions(assignments, participation)
    }

    @Test
    fun `details include assigned room and activity`() {
        Mockito.`when`(repository.findPersonById(11)).thenReturn(Fixtures.person())
        Mockito.`when`(assignments.findRoomByPersonId(11)).thenReturn(Fixtures.room())
        Mockito.`when`(participation.findActivityByPersonId(11)).thenReturn(Fixtures.activity())

        val body = request("/persons/11").json()
        val room = body.path("assignedRoom")
        val activity = body.path("activity")
        assertAll("person details and nested mapping",
            { assertThat(body.path("id").asLong()).isEqualTo(11) },
            { assertThat(body.path("registrationDate").asText()).isEqualTo("2026-09-15T10:30:00+02:00") },
            { assertThat(room.path("roomNumber").asInt()).isEqualTo(204) },
            { assertThat(room.path("size").asInt()).isEqualTo(23) },
            { assertThat(room.has("sizeSquareMeters")).isFalse() },
            { assertThat(room.path("lastRenovatedYear").asText()).isEqualTo("2024-01-01") },
            { assertThat(room.has("persons")).isFalse() },
            { assertThat(activity.path("id").asLong()).isEqualTo(1) },
            { assertThat(activity.path("startTime").asText()).isEqualTo("13.oct 09:00") },
            { assertThat(activity.path("endTime").asText()).isEqualTo("13.oct 15:00") },
            { assertThat(activity.path("notes").asText()).isEqualTo(Fixtures.activity().notes) },
            { assertThat(activity.has("participants")).isFalse() },
        )
    }

    @Test
    fun `person without relationships has no assigned room or activity`() {
        Mockito.`when`(repository.findPersonById(11)).thenReturn(Fixtures.person())
        val body = request("/persons/11").json()
        assertAll("optional relationships",
            { assertThat(body.path("assignedRoom").isNull || !body.has("assignedRoom")).isTrue() },
            { assertThat(body.path("activity").isNull || !body.has("activity")).isTrue() },
        )
    }

    @Test
    fun `missing person returns not found without loading relationships`() {
        request("/persons/999").expectEmpty(404)
        Mockito.verifyNoInteractions(assignments, participation)
    }

    @ParameterizedTest(name = "gender {0} is stored and returned as {1}")
    @CsvSource("0, 0", "1, 1", "2, 2", "3, 0", "-1, 0", "2147483647, 0", "-2147483648, 0")
    fun `creation normalizes gender and generates registration timestamp`(input: Int, expected: Int) {
        Mockito.`when`(repository.save(Mockito.any(Person::class.java))).thenAnswer {
            it.getArgument<Person>(0).apply { id = 11 }
        }
        val before = OffsetDateTime.now()
        val response = request("/persons", "POST", createBody(""", "gender": $input"""))
        val body = response.json(201)
        val captor = ArgumentCaptor.forClass(Person::class.java)
        Mockito.verify(repository).save(captor.capture())
        val saved = captor.value
        assertAll("created person",
            { assertThat(response.headers().firstValue("Location")).hasValue("/persons/11") },
            { assertThat(body.path("gender")).isEqualTo(parseJson(expected.toString())) },
            { assertThat(saved).usingRecursiveComparison().ignoringFields("registrationDate")
                .isEqualTo(Fixtures.person(gender = expected)) },
            { assertThat(saved.registrationDate).isBetween(before, OffsetDateTime.now()) },
            { assertThat(saved.registrationDate.offset).isEqualTo(ZoneOffset.UTC) },
            { assertThat(OffsetDateTime.parse(body.path("registrationDate").asText())).isEqualTo(saved.registrationDate) },
        )
    }

    @ParameterizedTest
    @ValueSource(strings = ["", ", \"gender\": null", ", \"gender\": \"mann\"", ", \"gender\": 2147483648"])
    fun `invalid gender input is rejected before persistence`(genderField: String) {
        request("/persons", "POST", createBody(genderField)).expectStatus(400)
        Mockito.verifyNoInteractions(repository)
    }

    @Test
    fun `invalid person fields are rejected before persistence`() {
        request("/persons", "POST", """{"name":"","department":"Test","email":"not-an-email","phoneNumber":"","gender":0}""")
            .expectStatus(400)
        Mockito.verifyNoInteractions(repository)
    }

    private fun createBody(genderField: String) =
        """{"name":"Test Person","department":"Test","email":"test@reiseapp.test","phoneNumber":"0000"$genderField}"""
}
