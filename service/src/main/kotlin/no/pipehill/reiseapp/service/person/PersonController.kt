package no.pipehill.reiseapp.service.person

import java.net.URI
import no.pipehill.reiseapp.api.PersonApi
import no.pipehill.reiseapp.api.dto.CreatePersonRequest
import no.pipehill.reiseapp.api.dto.PersonResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class PersonController(
    private val service: PersonService,
) : PersonApi {
    override fun createPerson(createPersonRequest: CreatePersonRequest): ResponseEntity<PersonResponse> {
        val createdPerson = service.add(createPersonRequest)
        return ResponseEntity
            .created(URI.create("/persons/${createdPerson.id}"))
            .body(createdPerson)
    }

    override fun getPerson(personId: Long): ResponseEntity<PersonResponse> =
        service.findById(personId)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    override fun listPersons(): ResponseEntity<List<PersonResponse>> =
        ResponseEntity.ok(service.findAll())
}
