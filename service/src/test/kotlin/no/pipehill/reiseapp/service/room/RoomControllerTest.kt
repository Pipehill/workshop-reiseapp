package no.pipehill.reiseapp.service.room

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import no.pipehill.reiseapp.api.dto.RoomResponse
import no.pipehill.reiseapp.api.dto.RoomDetailsResponse
import no.pipehill.reiseapp.api.dto.PersonResponse
import java.time.LocalDate
import no.pipehill.reiseapp.service.accommodation.PersonRoomRepository
import no.pipehill.reiseapp.service.person.PersonRepository
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
class RoomControllerTest {
    @MockitoBean
    private lateinit var service: RoomService

    @MockitoBean
    private lateinit var personRepository: PersonRepository

    @MockitoBean
    private lateinit var assignments: PersonRoomRepository

    @LocalServerPort
    private var port: Int = 0

    @MockitoBean
    private lateinit var accommodation: no.pipehill.reiseapp.service.accommodation.AccommodationService

    @MockitoBean
    private lateinit var participation: no.pipehill.reiseapp.service.activity.PersonActivityRepository

    @MockitoBean
    private lateinit var activityRepository: no.pipehill.reiseapp.service.activity.ActivityRepository

    private val mapper = ObjectMapper()

    @Test
    fun `lists rooms`() {
        Mockito.`when`(service.findAll()).thenReturn(listOf(roomResponse()))

        val response = sendGet("/rooms")

        assertThat(response.statusCode()).isEqualTo(200)
        assertThat(response.headers().firstValue("Content-Type").orElse(""))
            .startsWith("application/json")
        assertThat(mapper.readTree(response.body()))
            .isEqualTo(mapper.readTree(mapper.writeValueAsString(listOf(roomResponse()))))
    }

    @Test
    fun `gets a room or returns not found`() {
        val details = RoomDetailsResponse(204, 23, RoomDetailsResponse.NumberOfBeds._2, true, 2024,
            listOf(PersonResponse(11, "Test Person", "Test", "test@reiseapp.test",
                "+47 0000 0011", "mann", LocalDate.of(2026, 9, 15))))
        Mockito.`when`(service.findByNumber(204)).thenReturn(details)
        Mockito.`when`(service.findByNumber(999)).thenReturn(null)

        val response = sendGet("/rooms/204")

        assertThat(response.statusCode()).isEqualTo(200)
        assertThat(mapper.readTree(response.body()))
            .isEqualTo(mapper.readTree(mapper.writeValueAsString(details)))
        assertThat(sendGet("/rooms/999").statusCode()).isEqualTo(404)
    }

    @Test
    fun `rejects an invalid room number`() {
        assertThat(sendGet("/rooms/0").statusCode()).isEqualTo(400)
        Mockito.verifyNoInteractions(service)
    }

    private fun sendGet(path: String): HttpResponse<String> =
        HttpClient.newHttpClient().use { client ->
            val request = HttpRequest.newBuilder(URI("http://localhost:$port$path"))
                .header("Accept", "application/json")
                .GET()
                .build()
            client.send(request, HttpResponse.BodyHandlers.ofString())
        }

    private fun roomResponse(): RoomResponse =
        RoomResponse(
            roomNumber = 204,
            sizeSquareMeters = 23,
            numberOfBeds = RoomResponse.NumberOfBeds._2,
            hasBalcony = true,
            lastRenovatedYear = 2024,
        )
}
