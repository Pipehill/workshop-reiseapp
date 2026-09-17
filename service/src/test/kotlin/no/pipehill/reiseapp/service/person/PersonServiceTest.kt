package no.pipehill.reiseapp.service.person

import java.time.LocalDate
import no.pipehill.reiseapp.api.dto.CreatePersonRequest
import no.pipehill.reiseapp.api.dto.PersonResponse
import no.pipehill.reiseapp.service.accommodation.PersonRoomRepository
import no.pipehill.reiseapp.service.room.Room
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

    @Mock
    private lateinit var assignments: PersonRoomRepository

    private lateinit var service: PersonService

    @BeforeEach
    fun setUp() {
        service = PersonService(repository, assignments)
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
            .ignoringFields("registrationDate")
            .isEqualTo(person())
        assertThat(captor.value.registrationDate).isEqualTo(LocalDate.now())
    }

    @Test
    fun `maps one or all persons`() {
        val firstPerson = person(id = 1)
        val secondPerson = person(id = 2)
        Mockito.`when`(repository.findPersonById(1)).thenReturn(firstPerson)
        Mockito.`when`(repository.findPersonById(Long.MAX_VALUE)).thenReturn(null)
        Mockito.`when`(repository.findAll()).thenReturn(listOf(firstPerson, secondPerson))

        assertThat(service.findById(1)).usingRecursiveComparison()
            .ignoringFields("assignedRoom").isEqualTo(personResponse(id = 1))
        assertThat(service.findById(1)?.assignedRoom).isNull()
        assertThat(service.findById(Long.MAX_VALUE)).isNull()
        Mockito.verify(assignments, Mockito.never()).findRoomByPersonId(Long.MAX_VALUE)
        assertThat(service.findAll()).containsExactly(
            personResponse(id = 1),
            personResponse(id = 2),
        )
    }

    @Test
    fun `loads assigned room only for person details`() {
        Mockito.`when`(repository.findPersonById(1)).thenReturn(person(id = 1))
        Mockito.`when`(assignments.findRoomByPersonId(1)).thenReturn(Room(104, 22, 2, false, 2024))

        val result = service.findById(1)

        assertThat(result?.assignedRoom?.roomNumber).isEqualTo(104)
        assertThat(result?.assignedRoom?.sizeSquareMeters).isEqualTo(22)
        assertThat(result?.assignedRoom?.numberOfBeds?.value).isEqualTo(2)
        assertThat(result?.assignedRoom?.hasBalcony).isFalse()
        assertThat(result?.assignedRoom?.lastRenovatedYear).isEqualTo(2024)
        Mockito.clearInvocations(assignments)
        service.findAll()
        Mockito.verifyNoInteractions(assignments)
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
