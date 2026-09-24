package no.pipehill.reiseapp.service.activity

import no.pipehill.reiseapp.service.support.HttpApiTest
import no.pipehill.reiseapp.service.support.HttpTestSupport
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean

@HttpApiTest
@Import(ParticipationController::class)
class ParticipationControllerTest : HttpTestSupport() {
    @MockitoBean
    private lateinit var service: ParticipationService

    @Test
    fun `assignment returns success not found or conflict`() {
        for ((result, status) in listOf(
            ActivityEnrollmentResult.SUCCESS to 204,
            ActivityEnrollmentResult.PERSON_NOT_FOUND to 404,
            ActivityEnrollmentResult.ACTIVITY_NOT_FOUND to 404,
            ActivityEnrollmentResult.ACTIVITY_FULL to 409,
        )) {
            Mockito.`when`(service.enrollInActivity(1, 106)).thenReturn(result)
            val response = request("/persons/1/activity", "PUT", """{"activityId":106}""")
            assertAll("activity enrollment: $result", { response.expectEmpty(status) })
        }
    }

    @Test
    fun `removal returns success or missing person`() {
        Mockito.`when`(service.cancelActivityEnrollment(1)).thenReturn(ActivityEnrollmentResult.SUCCESS)
        Mockito.`when`(service.cancelActivityEnrollment(999)).thenReturn(ActivityEnrollmentResult.PERSON_NOT_FOUND)
        request("/persons/1/activity", "DELETE").expectStatus(204)
        request("/persons/999/activity", "DELETE").expectStatus(404)
    }

    @Test
    fun `invalid input never reaches service`() {
        for (body in listOf("{}", """{"activityId":0}""", """{"activityId":-1}""",
            """{"activityId":null}""", """{"activityId":"invalid"}""", "{", "")) {
            assertAll("invalid enrollment body: $body",
                { request("/persons/1/activity", "PUT", body).expectStatus(400) })
        }
        for (id in listOf("0", "-1", "abc")) {
            request("/persons/$id/activity", "PUT", """{"activityId":106}""").expectStatus(400)
            request("/persons/$id/activity", "DELETE").expectStatus(400)
        }
        Mockito.verifyNoInteractions(service)
    }
}
