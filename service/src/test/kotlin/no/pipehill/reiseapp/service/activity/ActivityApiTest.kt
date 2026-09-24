package no.pipehill.reiseapp.service.activity

import java.time.LocalDateTime
import no.pipehill.reiseapp.service.support.Fixtures
import no.pipehill.reiseapp.service.support.HttpApiTest
import no.pipehill.reiseapp.service.support.HttpTestSupport
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.Mockito
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean

@HttpApiTest
@Import(ActivityController::class, ActivityService::class)
class ActivityApiTest : HttpTestSupport() {
    @MockitoBean
    private lateinit var repository: ActivityRepository
    @MockitoBean
    private lateinit var participation: PersonActivityRepository

    @Test
    fun `list exposes activity fields and HTML without loading participants`() {
        Mockito.`when`(repository.findAll()).thenReturn(listOf(Fixtures.activity()))
        assertThat(request("/activities").json()).isEqualTo(parseJson("""[
            {"id":1,"title":"Fjelltur","description":"Tur med guide","maxParticipants":16,
             "startTime":"13.oct 09:00","endTime":"13.oct 15:00",
             "notes":"<p><strong>Gode sko.</strong><br>Mat &amp; vann.</p>"}
        ]"""))
        Mockito.verifyNoInteractions(participation)
    }

    @Test
    fun `details include participants without recursive relationships`() {
        Mockito.`when`(repository.findActivityById(1)).thenReturn(Fixtures.activity())
        Mockito.`when`(participation.findParticipantsByActivityId(1)).thenReturn(listOf(Fixtures.person()))
        val body = request("/activities/1").json()
        assertAll("activity details",
            { assertThat(body.path("id").asLong()).isEqualTo(1) },
            { assertThat(body.path("startTime").asText()).isEqualTo("13.oct 09:00") },
            { assertThat(body.path("endTime").asText()).isEqualTo("13.oct 15:00") },
            { assertThat(body.path("notes").asText()).isEqualTo(Fixtures.activity().notes) },
            { assertThat(body.path("participants").size()).isEqualTo(1) },
            { assertThat(body.path("participants").path(0).path("id").asLong()).isEqualTo(11) },
            { assertThat(body.path("participants").path(0).has("activity")).isFalse() },
        )
    }

    @Test
    fun `activity without participants returns an empty list`() {
        Mockito.`when`(repository.findActivityById(1)).thenReturn(Fixtures.activity())
        assertThat(request("/activities/1").json().path("participants")).isEqualTo(parseJson("[]"))
    }

    @ParameterizedTest(name = "{0} is formatted as {1}")
    @CsvSource(
        "2026-01-03T09:05:00, 3.jan 09:05",
        "2026-05-13T09:05:00, 13.may 09:05",
        "2026-09-30T09:05:00, 30.sep 09:05",
        "2026-12-31T09:05:00, 31.dec 09:05",
    )
    fun `formats dates with English months and preserves empty notes`(date: String, expected: String) {
        val activity = Fixtures.activity().apply {
            startTime = LocalDateTime.parse(date)
            endTime = startTime.plusHours(2)
            notes = ""
        }
        Mockito.`when`(repository.findAll()).thenReturn(listOf(activity))
        val body = request("/activities").json().path(0)
        assertAll("activity at $date",
            { assertThat(body.path("startTime").asText()).isEqualTo(expected) },
            { assertThat(body.path("notes").asText()).isEmpty() },
        )
    }

    @Test
    fun `empty list and missing activity`() {
        assertThat(request("/activities").json()).isEqualTo(parseJson("[]"))
        request("/activities/999").expectEmpty(404)
        Mockito.verifyNoInteractions(participation)
    }

    @Test
    fun `rejects invalid ids and writes`() {
        for (id in listOf("0", "-1", "abc")) {
            request("/activities/$id").expectStatus(400)
        }
        request("/activities", "POST").expectStatus(405)
        request("/activities/1", "PUT").expectStatus(405)
        Mockito.verifyNoInteractions(repository)
    }
}
