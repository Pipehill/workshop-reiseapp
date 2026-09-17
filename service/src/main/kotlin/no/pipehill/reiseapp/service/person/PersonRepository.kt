package no.pipehill.reiseapp.service.person

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.Lock
import jakarta.persistence.LockModeType
import org.springframework.data.repository.query.Param

interface PersonRepository : JpaRepository<Person, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT person FROM Person person WHERE person.id = :id")
    fun findPersonByIdForUpdate(@Param("id") id: Long): Person?

    @Query("SELECT person FROM Person person WHERE person.id = :id")
    fun findPersonById(@Param("id") id: Long): Person?

    @Query("SELECT person FROM Person person ORDER BY person.id")
    override fun findAll(): List<Person>
}
