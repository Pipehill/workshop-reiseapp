package no.pipehill.reiseapp.service.person

import jakarta.persistence.EntityManager
import java.time.OffsetDateTime
import no.pipehill.reiseapp.service.support.DatabaseTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException

@DatabaseTest
class PersonRepositoryTest {
    @Autowired
    private lateinit var repository: PersonRepository

    @Autowired
    private lateinit var entityManager: EntityManager

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
    fun `database rejects unsupported gender when bypassing service`() {
        assertThatThrownBy {
            repository.saveAndFlush(Person("Test Person", "Test", "invalid-gender@reiseapp.test", "0000", 3))
        }.isInstanceOf(DataIntegrityViolationException::class.java)
    }

    @Test
    fun `generates id and persists person fields and registration instant`() {
        val timestamp = OffsetDateTime.parse("2026-09-15T10:30:45.123456+02:00")
        val added = repository.saveAndFlush(
            Person("Timestamp Test", "Test", "timestamp@reiseapp.test", "0000", 1, timestamp),
        )
        assertThat(added.id).describedAs("generated person id").isNotNull().isPositive()
        entityManager.clear()

        val reloaded = checkNotNull(repository.findPersonById(checkNotNull(added.id)))
        assertThat(reloaded).usingRecursiveComparison().ignoringFields("registrationDate").isEqualTo(added)
        assertThat(reloaded.registrationDate.toInstant()).isEqualTo(timestamp.toInstant())
    }
}
