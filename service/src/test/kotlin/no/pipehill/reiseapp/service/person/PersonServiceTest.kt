package no.pipehill.reiseapp.service.person

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
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
    fun `adds a new person`() {
        val newPerson = person()
        val savedPerson = person(id = 11)
        Mockito.`when`(repository.save(newPerson)).thenReturn(savedPerson)

        assertThat(service.add(newPerson)).isSameAs(savedPerson)
        Mockito.verify(repository).save(newPerson)
    }

    @Test
    fun `rejects adding a person that already has an id`() {
        val existingPerson = person(id = 1)

        assertThatIllegalArgumentException()
            .isThrownBy { service.add(existingPerson) }
            .withMessage("A new person cannot already have an id")
        Mockito.verifyNoInteractions(repository)
    }

    @Test
    fun `finds one or all persons`() {
        val firstPerson = person(id = 1)
        val allPersons = listOf(firstPerson, person(id = 2))
        Mockito.`when`(repository.findPersonById(1)).thenReturn(firstPerson)
        Mockito.`when`(repository.findAll()).thenReturn(allPersons)

        assertThat(service.findById(1)).isSameAs(firstPerson)
        assertThat(service.findAll()).isSameAs(allPersons)
        Mockito.verify(repository).findPersonById(1)
        Mockito.verify(repository).findAll()
    }

    private fun person(id: Long? = null): Person =
        Person(
            name = "Vennlige Foss",
            department = "Test",
            email = "vennlige.foss@reiseapp.test",
            phoneNumber = "+47 0000 0011",
            gender = "mann",
            id = id,
        )
}
