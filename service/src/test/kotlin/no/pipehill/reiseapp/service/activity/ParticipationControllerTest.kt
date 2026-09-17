package no.pipehill.reiseapp.service.activity

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
class ParticipationControllerTest {
    @MockitoBean
    private lateinit var service: ParticipationService
    @MockitoBean
    private lateinit var persons: PersonService
    @MockitoBean
    private lateinit var rooms: RoomService
    @MockitoBean
    private lateinit var activities: ActivityService
    @LocalServerPort
    private var port: Int = 0

    @MockitoBean
    private lateinit var accommodation: no.pipehill.reiseapp.service.accommodation.AccommodationService

    @Test
    fun `assignment returns success not found or conflict`() {
        for ((result, status) in listOf(
            ActivityEnrollmentResult.SUCCESS to 204,
            ActivityEnrollmentResult.PERSON_NOT_FOUND to 404,
            ActivityEnrollmentResult.ACTIVITY_NOT_FOUND to 404,
            ActivityEnrollmentResult.ACTIVITY_FULL to 409,
        )) {
            Mockito.`when`(service.enrollInActivity(1, 106)).thenReturn(result)
            val response = request("PUT", "1", """{"activityId":106}""")
            assertThat(response.statusCode()).isEqualTo(status)
            assertThat(response.body()).isEmpty()
        }
    }

    @Test
    fun `removal returns success or missing person`() {
        Mockito.`when`(service.cancelActivityEnrollment(1)).thenReturn(ActivityEnrollmentResult.SUCCESS)
        Mockito.`when`(service.cancelActivityEnrollment(999)).thenReturn(ActivityEnrollmentResult.PERSON_NOT_FOUND)
        assertThat(request("DELETE", "1").statusCode()).isEqualTo(204)
        assertThat(request("DELETE", "999").statusCode()).isEqualTo(404)
    }

    @Test
    fun `invalid input never reaches service`() {
        for (body in listOf("{}", """{"activityId":0}""", """{"activityId":-1}""",
            """{"activityId":null}""", """{"activityId":"invalid"}""", "{", "")) {
            assertThat(request("PUT", "1", body).statusCode()).isEqualTo(400)
        }
        for (id in listOf("0", "-1", "abc")) {
            assertThat(request("PUT", id, """{"activityId":106}""").statusCode()).isEqualTo(400)
            assertThat(request("DELETE", id).statusCode()).isEqualTo(400)
        }
        Mockito.verifyNoInteractions(service)
    }

    private fun request(method: String, id: String, body: String = ""): HttpResponse<String> =
        HttpClient.newHttpClient().use { client ->
            client.send(
                HttpRequest.newBuilder(URI("http://localhost:$port/persons/$id/activity"))
                    .header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString(),
            )
        }
}
