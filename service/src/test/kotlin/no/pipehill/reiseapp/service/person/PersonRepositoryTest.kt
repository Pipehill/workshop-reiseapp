package no.pipehill.reiseapp.service.person

import java.time.OffsetDateTime
import jakarta.persistence.EntityManager
import no.pipehill.reiseapp.api.dto.CreatePersonRequest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.transaction.annotation.Transactional

@EnabledIfEnvironmentVariable(named = "REISEAPP_TEST_DATABASE_URL", matches = ".+")
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = [
        "spring.datasource.url=\${REISEAPP_TEST_DATABASE_URL}",
        "spring.datasource.username=\${REISEAPP_TEST_DATABASE_USER}",
        "spring.datasource.password=\${REISEAPP_TEST_DATABASE_PASSWORD}",
    ],
)
@Transactional
class PersonRepositoryTest {
    @Autowired
    private lateinit var repository: PersonRepository

    @Autowired
    private lateinit var entityManager: EntityManager

    @Autowired
    private lateinit var service: PersonService

    @Test
    fun `finds one or all persons`() {
        val person = repository.findPersonById(1)

        assertThat(person).isNotNull
        assertThat(person?.name).isEqualTo("Modige Fjell")
        assertThat(person?.department).isEqualTo("Plattform")
        assertThat(person?.email).isEqualTo("modige.fjell@reiseapp.test")
        assertThat(person?.phoneNumber).isEqualTo("+47 0000 0001")
        assertThat(person?.gender).isEqualTo(2)
        assertThat(repository.findPersonById(2)?.gender).isEqualTo(1)
        assertThat(repository.findPersonById(6)?.gender).isZero()
        assertThat(repository.findAll().map { it.gender }).containsOnly(0, 1, 2)
        assertThat(person?.registrationDate).isEqualTo(OffsetDateTime.parse("2026-01-15T10:30:00Z"))
        assertThat(repository.findPersonById(Long.MAX_VALUE)).isNull()

        val names = repository.findAll().map(Person::name)
        assertThat(names)
            .hasSize(20)
            .contains("Modige Fjell", "Nysgjerrige Glade Skog", "Tålmodige Bjørn")
    }

    @Test
    fun `saves and returns a person with generated fields`() {
        val before = OffsetDateTime.now()
        val added = repository.save(
            Person(
                name = "Vennlige Foss",
                department = "Test",
                email = "vennlige.foss@reiseapp.test",
                phoneNumber = "+47 0000 0011",
                gender = 1,
            ),
        )

        assertThat(added.id).isNotNull().isPositive()
        assertThat(added.registrationDate)
            .isBetween(before, OffsetDateTime.now())
        assertThat(repository.findPersonById(requireNotNull(added.id))).isSameAs(added)
        assertThat(repository.findAll()).contains(added)
    }

    @Test
    fun `persists normalized gender through service`() {
        val response = service.add(CreatePersonRequest("Test Person", "Test", "gender@reiseapp.test", "0000", -5))
        entityManager.flush()
        entityManager.clear()

        assertThat(response.gender).isZero()
        assertThat(repository.findPersonById(response.id)?.gender).isZero()
    }

    @Test
    fun `database rejects unsupported gender when bypassing service`() {
        assertThatThrownBy {
            repository.saveAndFlush(Person("Test Person", "Test", "invalid-gender@reiseapp.test", "0000", 3))
        }.isInstanceOf(DataIntegrityViolationException::class.java)
    }

    @Test
    fun `preserves registration instant after reloading a timestamp with an offset`() {
        val timestamp = OffsetDateTime.parse("2026-09-15T10:30:45.123456+02:00")
        val added = repository.saveAndFlush(
            Person("Timestamp Test", "Test", "timestamp@reiseapp.test", "0000", 1, timestamp),
        )
        entityManager.clear()

        val reloaded = checkNotNull(repository.findPersonById(checkNotNull(added.id)))
        assertThat(reloaded.registrationDate.toInstant()).isEqualTo(timestamp.toInstant())
    }
}
