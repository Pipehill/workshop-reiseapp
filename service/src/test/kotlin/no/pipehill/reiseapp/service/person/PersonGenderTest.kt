package no.pipehill.reiseapp.service.person

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import no.pipehill.reiseapp.service.accommodation.PersonRoomRepository
import no.pipehill.reiseapp.service.activity.ActivityRepository
import no.pipehill.reiseapp.service.activity.PersonActivityRepository
import no.pipehill.reiseapp.service.room.RoomRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.ArgumentCaptor
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
class PersonGenderTest {
    @MockitoBean
    private lateinit var repository: PersonRepository
    @MockitoBean
    private lateinit var rooms: RoomRepository
    @MockitoBean
    private lateinit var assignments: PersonRoomRepository
    @MockitoBean
    private lateinit var activities: ActivityRepository
    @MockitoBean
    private lateinit var participation: PersonActivityRepository
    @LocalServerPort
    private var port: Int = 0

    private val mapper = ObjectMapper()

    @ParameterizedTest
    @CsvSource("0, 0", "1, 1", "2, 2", "3, 0", "-1, 0", "2147483647, 0", "-2147483648, 0")
    fun `normalizes gender before saving and returns an integer`(input: Int, expected: Int) {
        Mockito.`when`(repository.save(Mockito.any(Person::class.java))).thenAnswer {
            it.getArgument<Person>(0).apply { id = 21 }
        }

        val response = createPerson(""", "gender": $input""")

        assertThat(response.statusCode()).isEqualTo(201)
        val gender = mapper.readTree(response.body()).get("gender")
        assertThat(gender.isIntegralNumber).isTrue()
        assertThat(gender.asInt()).isEqualTo(expected)
        val captor = ArgumentCaptor.forClass(Person::class.java)
        Mockito.verify(repository).save(captor.capture())
        assertThat(captor.value.gender).isEqualTo(expected)
    }

    @ParameterizedTest
    @ValueSource(strings = ["", ", \"gender\": null", ", \"gender\": \"mann\"", ", \"gender\": 2147483648"])
    fun `rejects missing null text and out of range gender`(genderField: String) {
        assertThat(createPerson(genderField).statusCode()).isEqualTo(400)
        Mockito.verifyNoInteractions(repository)
    }

    private fun createPerson(genderField: String): HttpResponse<String> =
        HttpClient.newHttpClient().use { client ->
            val body = """{"name":"Test Person","department":"Test","email":"gender@reiseapp.test","phoneNumber":"0000"$genderField}"""
            client.send(
                HttpRequest.newBuilder(URI("http://localhost:$port/persons"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build(),
                HttpResponse.BodyHandlers.ofString(),
            )
        }
}
