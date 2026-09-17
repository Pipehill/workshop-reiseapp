package no.pipehill.reiseapp.service.activity

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.OffsetDateTime
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
    private val mapper = ObjectMapper()

    @Test
    fun `lists activities and maps all details through service`() {
        val start = OffsetDateTime.parse("2026-10-03T09:00:00+02:00")
        val activity = Activity("Fjelltur", "Tur med guide", 16, start, start.plusHours(6),
            "Gode sko.\nMat og vann.", 1)
        Mockito.`when`(repository.findAll()).thenReturn(listOf(activity))
        Mockito.`when`(repository.findActivityById(1)).thenReturn(activity)

        val response = request("/activities/1")
        assertThat(response.statusCode()).isEqualTo(200)
        assertThat(response.headers().firstValue("Content-Type").orElse("")).startsWith("application/json")
        val body = mapper.readTree(response.body())
        assertThat(body.get("id").asLong()).isEqualTo(1)
        assertThat(body.get("title").asText()).isEqualTo("Fjelltur")
        assertThat(body.get("description").asText()).isEqualTo("Tur med guide")
        assertThat(body.get("maxParticipants").asInt()).isEqualTo(16)
        assertThat(OffsetDateTime.parse(body.get("startTime").asText()).toInstant()).isEqualTo(start.toInstant())
        assertThat(OffsetDateTime.parse(body.get("endTime").asText()).toInstant()).isEqualTo(start.plusHours(6).toInstant())
        assertThat(body.get("notes").asText()).isEqualTo("Gode sko.\nMat og vann.")
        assertThat(body.has("checklist")).isFalse()
        val list = request("/activities")
        assertThat(list.statusCode()).isEqualTo(200)
        assertThat(mapper.readTree(list.body())).isEqualTo(mapper.readTree("[" + response.body() + "]"))
    }

    @Test
    fun `empty list and missing activity`() {
        assertThat(mapper.readTree(request("/activities").body())).isEqualTo(mapper.readTree("[]"))
        assertThat(request("/activities/999").statusCode()).isEqualTo(404)
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
