package no.pipehill.reiseapp.service.accommodation

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import no.pipehill.reiseapp.service.person.PersonService
import no.pipehill.reiseapp.service.room.RoomService
import no.pipehill.reiseapp.service.activity.ActivityService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.bean.override.mockito.MockitoBean

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "spring.autoconfigure.exclude=" +
            "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration," +
            "org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration",
    ],
)
class AccommodationControllerTest {
    @MockitoBean
    private lateinit var service: AccommodationService
    @MockitoBean
    private lateinit var persons: PersonService
    @MockitoBean
    private lateinit var rooms: RoomService
    @MockitoBean
    private lateinit var activities: ActivityService
    @LocalServerPort
    private var port: Int = 0

    @Test
    fun `assignment returns success not found or conflict`() {
        for ((result, status) in listOf(
            RoomAssignmentResult.SUCCESS to 204,
            RoomAssignmentResult.PERSON_NOT_FOUND to 404,
            RoomAssignmentResult.ROOM_NOT_FOUND to 404,
            RoomAssignmentResult.ROOM_FULL to 409,
        )) {
            Mockito.`when`(service.assignRoom(1, 106)).thenReturn(result)
            val response = request("PUT", "1", """{"roomNumber":106}""")
            assertThat(response.statusCode()).isEqualTo(status)
            assertThat(response.body()).isEmpty()
        }
    }

    @Test
    fun `removal returns success or missing person`() {
        Mockito.`when`(service.removeRoomAssignment(1)).thenReturn(RoomAssignmentResult.SUCCESS)
        Mockito.`when`(service.removeRoomAssignment(999)).thenReturn(RoomAssignmentResult.PERSON_NOT_FOUND)
        assertThat(request("DELETE", "1").statusCode()).isEqualTo(204)
        assertThat(request("DELETE", "999").statusCode()).isEqualTo(404)
    }

    @Test
    fun `invalid input never reaches service`() {
        for (body in listOf("{}", """{"roomNumber":0}""", """{"roomNumber":-1}""",
            """{"roomNumber":null}""", """{"roomNumber":"invalid"}""", "{", "")) {
            assertThat(request("PUT", "1", body).statusCode()).isEqualTo(400)
        }
        for (id in listOf("0", "-1", "abc")) {
            assertThat(request("PUT", id, """{"roomNumber":106}""").statusCode()).isEqualTo(400)
            assertThat(request("DELETE", id).statusCode()).isEqualTo(400)
        }
        Mockito.verifyNoInteractions(service)
    }

    private fun request(method: String, id: String, body: String = ""): HttpResponse<String> =
        HttpClient.newHttpClient().use { client ->
            client.send(
                HttpRequest.newBuilder(URI("http://localhost:$port/persons/$id/room"))
                    .header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString(),
            )
        }
}
