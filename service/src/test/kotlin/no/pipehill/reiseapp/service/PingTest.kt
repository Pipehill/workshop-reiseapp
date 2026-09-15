package no.pipehill.reiseapp.service

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import tools.jackson.databind.ObjectMapper

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "spring.autoconfigure.exclude=" +
            "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration," +
            "org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration",
    ],
)
class PingTest {
    @LocalServerPort
    private var port: Int = 0

    @Test
    fun `ping returns pong as JSON without authentication`() {
        HttpClient.newHttpClient().use { client ->
            val request = HttpRequest.newBuilder(URI("http://localhost:$port/ping"))
                .header("Accept", "application/json")
                .GET()
                .build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())

            assertThat(response.statusCode()).isEqualTo(200)
            assertThat(response.headers().firstValue("Content-Type").orElse(""))
                .startsWith("application/json")
            val mapper = ObjectMapper()
            assertThat(mapper.readTree(response.body()))
                .isEqualTo(mapper.readTree("""{"message":"pong"}"""))
        }
    }
}
