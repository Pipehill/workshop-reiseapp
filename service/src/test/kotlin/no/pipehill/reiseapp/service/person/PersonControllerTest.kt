package no.pipehill.reiseapp.service.person

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDate
import no.pipehill.reiseapp.api.dto.CreatePersonRequest
import no.pipehill.reiseapp.api.dto.PersonResponse
import no.pipehill.reiseapp.api.dto.PersonDetailsResponse
import no.pipehill.reiseapp.api.dto.RoomResponse
import no.pipehill.reiseapp.service.accommodation.PersonRoomRepository
import no.pipehill.reiseapp.service.room.RoomRepository
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
class PersonControllerTest {
    @MockitoBean
    private lateinit var service: PersonService

    @MockitoBean
    private lateinit var roomRepository: RoomRepository

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

    @MockitoBean
    private lateinit var activityRepository: no.pipehill.reiseapp.service.activity.ActivityRepository

    private val mapper = ObjectMapper()

    @Test
    fun `lists persons`() {
        Mockito.`when`(service.findAll()).thenReturn(listOf(personResponse()))

        val response = sendGet("/persons")

        assertThat(response.statusCode()).isEqualTo(200)
        assertThat(response.headers().firstValue("Content-Type").orElse(""))
            .startsWith("application/json")
        assertThat(mapper.readTree(response.body()))
            .isEqualTo(mapper.readTree(mapper.writeValueAsString(listOf(personResponse()))))
    }

    @Test
    fun `gets a person or returns not found`() {
        val details = personResponse().let {
            PersonDetailsResponse(it.id, it.name, it.department, it.email, it.phoneNumber,
                it.gender, it.registrationDate,
                assignedRoom = RoomResponse(104, 22, RoomResponse.NumberOfBeds._2, false, 2024))
        }
        Mockito.`when`(service.findById(11)).thenReturn(details)
        Mockito.`when`(service.findById(12)).thenReturn(null)

        val response = sendGet("/persons/11")
        assertThat(response.statusCode()).isEqualTo(200)
        assertThat(mapper.readTree(response.body()).get("assignedRoom").get("roomNumber").asInt())
            .isEqualTo(104)
        assertThat(sendGet("/persons/12").statusCode()).isEqualTo(404)
    }

    @Test
    fun `creates a person`() {
        val requestDto = createRequest()
        Mockito.`when`(service.add(requestDto)).thenReturn(personResponse())

        val response = HttpClient.newHttpClient().use { client ->
            val request = HttpRequest.newBuilder(URI("http://localhost:$port/persons"))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(requestDto)))
                .build()
            client.send(request, HttpResponse.BodyHandlers.ofString())
        }

        assertThat(response.statusCode()).isEqualTo(201)
        assertThat(response.headers().firstValue("Location")).hasValue("/persons/11")
        assertThat(mapper.readTree(response.body()))
            .isEqualTo(mapper.readTree(mapper.writeValueAsString(personResponse())))
        Mockito.verify(service).add(requestDto)
    }

    @Test
    fun `rejects an invalid person`() {
        val response = HttpClient.newHttpClient().use { client ->
            val request = HttpRequest.newBuilder(URI("http://localhost:$port/persons"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""{"name":"","department":"Test","email":"not-an-email","phoneNumber":"","gender":""}"""))
                .build()
            client.send(request, HttpResponse.BodyHandlers.ofString())
        }

        assertThat(response.statusCode()).isEqualTo(400)
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

    private fun createRequest(): CreatePersonRequest =
        CreatePersonRequest(
            name = "Vennlige Foss",
            department = "Test",
            email = "vennlige.foss@reiseapp.test",
            phoneNumber = "+47 0000 0011",
            gender = "mann",
        )

    private fun personResponse(): PersonResponse =
        PersonResponse(
            id = 11,
            name = "Vennlige Foss",
            department = "Test",
            email = "vennlige.foss@reiseapp.test",
            phoneNumber = "+47 0000 0011",
            gender = "mann",
            registrationDate = LocalDate.of(2026, 9, 15),
        )
}
