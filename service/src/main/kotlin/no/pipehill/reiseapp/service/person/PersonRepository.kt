package no.pipehill.reiseapp.service.person

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PersonRepository : JpaRepository<Person, Long> {
    @Query("SELECT person FROM Person person WHERE person.id = :id")
    fun findPersonById(@Param("id") id: Long): Person?

    @Query("SELECT person FROM Person person ORDER BY person.id")
    override fun findAll(): List<Person>
}
