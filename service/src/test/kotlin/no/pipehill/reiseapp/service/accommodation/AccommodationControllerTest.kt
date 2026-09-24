package no.pipehill.reiseapp.service.accommodation

import no.pipehill.reiseapp.service.support.HttpApiTest
import no.pipehill.reiseapp.service.support.HttpTestSupport
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean

@HttpApiTest
@Import(AccommodationController::class)
class AccommodationControllerTest : HttpTestSupport() {
    @MockitoBean
    private lateinit var service: AccommodationService

    @Test
    fun `assignment returns success not found or conflict`() {
        for ((result, status) in listOf(
            RoomAssignmentResult.SUCCESS to 204,
            RoomAssignmentResult.PERSON_NOT_FOUND to 404,
            RoomAssignmentResult.ROOM_NOT_FOUND to 404,
            RoomAssignmentResult.ROOM_FULL to 409,
        )) {
            Mockito.`when`(service.assignRoom(1, 106)).thenReturn(result)
            val response = request("/persons/1/room", "PUT", """{"roomNumber":106}""")
            assertAll("room assignment: $result", { response.expectEmpty(status) })
        }
    }

    @Test
    fun `removal returns success or missing person`() {
        Mockito.`when`(service.removeRoomAssignment(1)).thenReturn(RoomAssignmentResult.SUCCESS)
        Mockito.`when`(service.removeRoomAssignment(999)).thenReturn(RoomAssignmentResult.PERSON_NOT_FOUND)
        request("/persons/1/room", "DELETE").expectStatus(204)
        request("/persons/999/room", "DELETE").expectStatus(404)
    }

    @Test
    fun `invalid input never reaches service`() {
        for (body in listOf("{}", """{"roomNumber":0}""", """{"roomNumber":-1}""",
            """{"roomNumber":null}""", """{"roomNumber":"invalid"}""", "{", "")) {
            assertAll("invalid room assignment body: $body",
                { request("/persons/1/room", "PUT", body).expectStatus(400) })
        }
        for (id in listOf("0", "-1", "abc")) {
            request("/persons/$id/room", "PUT", """{"roomNumber":106}""").expectStatus(400)
            request("/persons/$id/room", "DELETE").expectStatus(400)
        }
        Mockito.verifyNoInteractions(service)
    }
}
