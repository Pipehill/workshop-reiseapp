package no.pipehill.reiseapp.service.person

import no.pipehill.reiseapp.api.dto.CreatePersonRequest
import no.pipehill.reiseapp.api.dto.PersonResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PersonService(
    private val repository: PersonRepository,
) {
    @Transactional
    fun add(request: CreatePersonRequest): PersonResponse =
        repository.save(request.toEntity()).toResponse()

    @Transactional(readOnly = true)
    fun findById(id: Long): PersonResponse? = repository.findPersonById(id)?.toResponse()

    @Transactional(readOnly = true)
    fun findAll(): List<PersonResponse> = repository.findAll().map { it.toResponse() }

    private fun CreatePersonRequest.toEntity(): Person =
        Person(
            name = name,
            department = department,
            email = email,
            phoneNumber = phoneNumber,
            gender = gender,
        )

    private fun Person.toResponse(): PersonResponse =
        PersonResponse(
            id = checkNotNull(id) { "A persisted person must have an id" },
            name = name,
            department = department,
            email = email,
            phoneNumber = phoneNumber,
            gender = gender,
            registrationDate = registrationDate,
        )
}
