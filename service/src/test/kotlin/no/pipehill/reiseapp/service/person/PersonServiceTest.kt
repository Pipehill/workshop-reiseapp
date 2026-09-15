package no.pipehill.reiseapp.service.person

import java.time.LocalDate
import no.pipehill.reiseapp.api.dto.CreatePersonRequest
import no.pipehill.reiseapp.api.dto.PersonResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class PersonServiceTest {
    @Mock
    private lateinit var repository: PersonRepository

    private lateinit var service: PersonService

    @BeforeEach
    fun setUp() {
        service = PersonService(repository)
    }

    @Test
    fun `maps and adds a new person`() {
        val request = createRequest()
        val savedPerson = person(id = 11)
        Mockito.`when`(repository.save(Mockito.any(Person::class.java))).thenReturn(savedPerson)

        assertThat(service.add(request)).isEqualTo(personResponse(id = 11))

        val captor = ArgumentCaptor.forClass(Person::class.java)
        Mockito.verify(repository).save(captor.capture())
        assertThat(captor.value)
            .usingRecursiveComparison()
            .isEqualTo(person())
    }

    @Test
    fun `maps one or all persons`() {
        val firstPerson = person(id = 1)
        val secondPerson = person(id = 2)
        Mockito.`when`(repository.findPersonById(1)).thenReturn(firstPerson)
        Mockito.`when`(repository.findPersonById(Long.MAX_VALUE)).thenReturn(null)
        Mockito.`when`(repository.findAll()).thenReturn(listOf(firstPerson, secondPerson))

        assertThat(service.findById(1)).isEqualTo(personResponse(id = 1))
        assertThat(service.findById(Long.MAX_VALUE)).isNull()
        assertThat(service.findAll()).containsExactly(
            personResponse(id = 1),
            personResponse(id = 2),
        )
    }

    private fun createRequest(): CreatePersonRequest =
        CreatePersonRequest(
            name = "Vennlige Foss",
            department = "Test",
            email = "vennlige.foss@reiseapp.test",
            phoneNumber = "+47 0000 0011",
            gender = "mann",
        )

    private fun person(id: Long? = null): Person =
        Person(
            name = "Vennlige Foss",
            department = "Test",
            email = "vennlige.foss@reiseapp.test",
            phoneNumber = "+47 0000 0011",
            gender = "mann",
            registrationDate = LocalDate.of(2026, 9, 15),
            id = id,
        )

    private fun personResponse(id: Long): PersonResponse =
        PersonResponse(
            id = id,
            name = "Vennlige Foss",
            department = "Test",
            email = "vennlige.foss@reiseapp.test",
            phoneNumber = "+47 0000 0011",
            gender = "mann",
            registrationDate = LocalDate.of(2026, 9, 15),
        )
}
