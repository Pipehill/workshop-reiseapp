package no.pipehill.reiseapp.service.activity

import no.pipehill.reiseapp.service.person.Person
import org.springframework.data.repository.Repository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PersonActivityRepository : Repository<PersonActivity, Long> {
    @Query("SELECT activity FROM Activity activity JOIN PersonActivity participation ON participation.activityId = activity.id WHERE participation.personId = :personId")
    fun findActivityByPersonId(@Param("personId") personId: Long): Activity?

    @Query("SELECT person FROM Person person JOIN PersonActivity participation ON participation.personId = person.id WHERE participation.activityId = :activityId ORDER BY person.id")
    fun findParticipantsByActivityId(@Param("activityId") activityId: Long): List<Person>
}
