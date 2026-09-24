package no.pipehill.reiseapp.service.support

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.URI
import no.pipehill.reiseapp.service.ApiExceptionHandler
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertAll
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestComponent
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@SpringBootTest(
    classes = [HttpTestConfiguration::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "spring.autoconfigure.exclude=" +
            "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration," +
            "org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration",
    ],
)
annotation class HttpApiTest

// Each test imports only its own controller and, for read APIs, its service.
@Configuration(proxyBeanMethods = false)
@TestComponent
@EnableAutoConfiguration
@Import(ApiExceptionHandler::class)
class HttpTestConfiguration

abstract class HttpTestSupport {
    @LocalServerPort
    private var port: Int = 0
    private val client = HttpClient.newHttpClient()
    private val mapper = ObjectMapper()

    @AfterEach
    fun closeClient() = client.close()

    protected fun request(path: String, method: String = "GET", body: String = ""): HttpResponse<String> =
        client.send(
            HttpRequest.newBuilder(URI("http://localhost:$port$path"))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .method(method, HttpRequest.BodyPublishers.ofString(body)).build(),
            HttpResponse.BodyHandlers.ofString(),
        )

    protected fun HttpResponse<String>.expectStatus(status: Int): HttpResponse<String> = apply {
        assertThat(statusCode()).`as`("%s %s: %s", request().method(), uri().path, body()).isEqualTo(status)
    }

    protected fun HttpResponse<String>.json(status: Int = 200): JsonNode {
        expectStatus(status)
        assertThat(headers().firstValue("Content-Type").orElse(""))
            .`as`("Content-Type for %s", uri().path).startsWith("application/json")
        return parseJson(body())
    }

    protected fun parseJson(json: String): JsonNode = mapper.readTree(json)

    protected fun HttpResponse<String>.expectEmpty(status: Int) = assertAll(
        "${request().method()} ${uri().path}",
        { expectStatus(status) },
        { assertThat(body()).`as`("response body").isEmpty() },
    )
}
