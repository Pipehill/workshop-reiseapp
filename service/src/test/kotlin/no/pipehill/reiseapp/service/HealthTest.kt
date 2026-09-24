package no.pipehill.reiseapp.service

import no.pipehill.reiseapp.service.support.HttpApiTest
import no.pipehill.reiseapp.service.support.HttpTestSupport
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Import

@HttpApiTest
@Import(HealthController::class)
class HealthTest : HttpTestSupport() {
    @Test
    fun `health returns UP without authentication or a database`() {
        assertThat(request("/health").json()).isEqualTo(parseJson("""{"status":"UP"}"""))
    }

    @Test
    fun `old ping endpoint is removed`() {
        request("/ping").expectStatus(404)
    }
}
