package no.pipehill.reiseapp.service.person

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PersonService(
    private val repository: PersonRepository,
) {
    @Transactional
    fun add(person: Person): Person {
        require(person.id == null) { "A new person cannot already have an id" }
        return repository.save(person)
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): Person? = repository.findPersonById(id)

    @Transactional(readOnly = true)
    fun findAll(): List<Person> = repository.findAll()
}
