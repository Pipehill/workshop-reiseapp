package no.pipehill.reiseapp.service

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import no.pipehill.reiseapp.service.person.PersonRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
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
class HealthTest {
    @MockitoBean
    private lateinit var personRepository: PersonRepository

    @LocalServerPort
    private var port: Int = 0

    @Test
    fun `health returns UP as JSON without authentication or a database`() {
        HttpClient.newHttpClient().use { client ->
            val request = HttpRequest.newBuilder(URI("http://localhost:$port/health"))
                .header("Accept", "application/json")
                .GET()
                .build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())

            assertThat(response.statusCode()).isEqualTo(200)
            assertThat(response.headers().firstValue("Content-Type").orElse(""))
                .startsWith("application/json")
            val mapper = ObjectMapper()
            assertThat(mapper.readTree(response.body()))
                .isEqualTo(mapper.readTree("""{"status":"UP"}"""))
        }
    }

    @Test
    fun `old ping endpoint is removed`() {
        HttpClient.newHttpClient().use { client ->
            val request = HttpRequest.newBuilder(URI("http://localhost:$port/ping"))
                .GET()
                .build()

            val response = client.send(request, HttpResponse.BodyHandlers.discarding())

            assertThat(response.statusCode()).isEqualTo(404)
        }
    }
}
