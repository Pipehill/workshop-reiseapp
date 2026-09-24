package no.pipehill.reiseapp.service.room

import no.pipehill.reiseapp.service.accommodation.PersonRoomRepository
import no.pipehill.reiseapp.service.support.Fixtures
import no.pipehill.reiseapp.service.support.HttpApiTest
import no.pipehill.reiseapp.service.support.HttpTestSupport
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean

@HttpApiTest
@Import(RoomController::class, RoomService::class)
class RoomApiTest : HttpTestSupport() {
    @MockitoBean
    private lateinit var repository: RoomRepository
    @MockitoBean
    private lateinit var assignments: PersonRoomRepository

    @Test
    fun `list exposes room fields without loading occupants`() {
        Mockito.`when`(repository.findAll()).thenReturn(listOf(Fixtures.room()))
        assertThat(request("/rooms").json()).isEqualTo(parseJson("""[
            {"roomNumber":204,"size":23,"numberOfBeds":2,"hasBalcony":true,"lastRenovatedYear":"2024-01-01"}
        ]"""))
        Mockito.verifyNoInteractions(assignments)
    }

    @Test
    fun `details include occupants without recursive relationships`() {
        Mockito.`when`(repository.findRoomByNumber(204)).thenReturn(Fixtures.room())
        Mockito.`when`(assignments.findPersonsByRoomNumber(204)).thenReturn(listOf(Fixtures.person()))
        val body = request("/rooms/204").json()
        assertAll("room details",
            { assertThat(body.path("roomNumber").asInt()).isEqualTo(204) },
            { assertThat(body.path("size").asInt()).isEqualTo(23) },
            { assertThat(body.has("sizeSquareMeters")).isFalse() },
            { assertThat(body.path("lastRenovatedYear").asText()).isEqualTo("2024-01-01") },
            { assertThat(body.path("persons").size()).isEqualTo(1) },
            { assertThat(body.path("persons").path(0).path("id").asLong()).isEqualTo(11) },
            { assertThat(body.path("persons").path(0).has("assignedRoom")).isFalse() },
        )
    }

    @Test
    fun `unoccupied room returns an empty occupant list`() {
        Mockito.`when`(repository.findRoomByNumber(204)).thenReturn(Fixtures.room())
        assertThat(request("/rooms/204").json().path("persons")).isEqualTo(parseJson("[]"))
    }

    @Test
    fun `missing and invalid room numbers are rejected`() {
        request("/rooms/999").expectEmpty(404)
        Mockito.verifyNoInteractions(assignments)
        Mockito.clearInvocations(repository)
        request("/rooms/0").expectStatus(400)
        Mockito.verifyNoInteractions(repository)
    }
}
