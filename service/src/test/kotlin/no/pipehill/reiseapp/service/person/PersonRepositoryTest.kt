package no.pipehill.reiseapp.service.person

import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
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

    @Test
    fun `finds one or all persons`() {
        val person = repository.findPersonById(1)

        assertThat(person).isNotNull
        assertThat(person?.name).isEqualTo("Modige Fjell")
        assertThat(person?.department).isEqualTo("Plattform")
        assertThat(person?.email).isEqualTo("modige.fjell@reiseapp.test")
        assertThat(person?.phoneNumber).isEqualTo("+47 0000 0001")
        assertThat(person?.gender).isEqualTo("kvinne")
        assertThat(person?.registrationDate).isEqualTo(LocalDate.of(2026, 1, 15))
        assertThat(repository.findPersonById(Long.MAX_VALUE)).isNull()

        val names = repository.findAll().map(Person::name)
        assertThat(names)
            .hasSize(10)
            .contains("Modige Fjell", "Nysgjerrige Glade Skog", "Tålmodige Bjørn")
    }

    @Test
    fun `saves and returns a person with generated fields`() {
        val added = repository.save(
            Person(
                name = "Vennlige Foss",
                department = "Test",
                email = "vennlige.foss@reiseapp.test",
                phoneNumber = "+47 0000 0011",
                gender = "mann",
            ),
        )

        assertThat(added.id).isNotNull().isPositive()
        assertThat(added.registrationDate)
            .isBetween(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1))
        assertThat(repository.findPersonById(requireNotNull(added.id))).isSameAs(added)
        assertThat(repository.findAll()).contains(added)
    }
}
