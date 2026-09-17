package no.pipehill.reiseapp.service.activity

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalTime
import no.pipehill.reiseapp.service.person.PersonRepository
import no.pipehill.reiseapp.service.room.RoomRepository
import no.pipehill.reiseapp.service.accommodation.PersonRoomRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.bean.override.mockito.MockitoBean
import tools.jackson.databind.ObjectMapper

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "spring.autoconfigure.exclude=" +
            "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration," +
            "org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration",
    ],
)
class ActivityControllerTest {
    @MockitoBean
    private lateinit var repository: ActivityRepository
    @MockitoBean
    private lateinit var persons: PersonRepository
    @MockitoBean
    private lateinit var rooms: RoomRepository
    @MockitoBean
    private lateinit var assignments: PersonRoomRepository
    @LocalServerPort
    private var port: Int = 0

    @MockitoBean
    private lateinit var enrollment: no.pipehill.reiseapp.service.activity.ParticipationService

    @MockitoBean
    private lateinit var accommodation: no.pipehill.reiseapp.service.accommodation.AccommodationService

    @MockitoBean
    private lateinit var participation: no.pipehill.reiseapp.service.activity.PersonActivityRepository
    private val mapper = ObjectMapper()

    @Test
    fun `lists activities and maps all details through service`() {
        val start = LocalTime.parse("09:00:00")
        val activity = Activity("Fjelltur", "Tur med guide", 16, start, start.plusHours(6),
            "Gode sko.\nMat og vann.", 1)
        Mockito.`when`(repository.findAll()).thenReturn(listOf(activity))
        Mockito.`when`(repository.findActivityById(1)).thenReturn(activity)
        Mockito.`when`(participation.findParticipantsByActivityId(1)).thenReturn(listOf(
            no.pipehill.reiseapp.service.person.Person(
                "Test Person", "Test", "test@reiseapp.test", "+47 0000 0011", "mann", id = 11)))

        val response = request("/activities/1")
        assertThat(response.statusCode()).isEqualTo(200)
        assertThat(response.headers().firstValue("Content-Type").orElse("")).startsWith("application/json")
        val body = mapper.readTree(response.body())
        assertThat(body.get("id").asLong()).isEqualTo(1)
        assertThat(body.get("title").asText()).isEqualTo("Fjelltur")
        assertThat(body.get("description").asText()).isEqualTo("Tur med guide")
        assertThat(body.get("maxParticipants").asInt()).isEqualTo(16)
        assertThat(body.get("startTime").asText()).isEqualTo("09:00:00")
        assertThat(body.get("endTime").asText()).isEqualTo("15:00:00")
        assertThat(body.get("notes").asText()).isEqualTo("Gode sko.\nMat og vann.")
        assertThat(body.has("checklist")).isFalse()
        assertThat(body.get("participants").size()).isEqualTo(1)
        assertThat(body.get("participants").get(0).get("id").asLong()).isEqualTo(11)
        assertThat(body.get("participants").get(0).has("activity")).isFalse()
        Mockito.clearInvocations(participation)
        val list = request("/activities")
        assertThat(list.statusCode()).isEqualTo(200)
        assertThat(mapper.readTree(list.body()).get(0).has("participants")).isFalse()
        assertThat(mapper.readTree(list.body()).get(0).get("title")).isEqualTo(body.get("title"))
        assertThat(mapper.readTree(list.body()).get(0).get("startTime").asText()).isEqualTo("09:00:00")
        assertThat(mapper.readTree(list.body()).get(0).get("endTime").asText()).isEqualTo("15:00:00")
        Mockito.verifyNoInteractions(participation)
    }

    @Test
    fun `empty list and missing activity`() {
        assertThat(mapper.readTree(request("/activities").body())).isEqualTo(mapper.readTree("[]"))
        assertThat(request("/activities/999").statusCode()).isEqualTo(404)
        Mockito.verifyNoInteractions(participation)
    }

    @Test
    fun `rejects invalid ids and writes`() {
        for (id in listOf("0", "-1", "abc")) {
            assertThat(request("/activities/$id").statusCode()).isEqualTo(400)
        }
        assertThat(request("/activities", "POST").statusCode()).isEqualTo(405)
        assertThat(request("/activities/1", "PUT").statusCode()).isEqualTo(405)
        Mockito.verifyNoInteractions(repository)
    }

    private fun request(path: String, method: String = "GET"): HttpResponse<String> =
        HttpClient.newHttpClient().use { client ->
            client.send(
                HttpRequest.newBuilder(URI("http://localhost:$port$path"))
                    .header("Accept", "application/json")
                    .method(method, HttpRequest.BodyPublishers.noBody()).build(),
                HttpResponse.BodyHandlers.ofString(),
            )
        }
}
