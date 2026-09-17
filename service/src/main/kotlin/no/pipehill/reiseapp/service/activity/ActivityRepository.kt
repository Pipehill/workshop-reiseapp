package no.pipehill.reiseapp.service.activity

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ActivityRepository : JpaRepository<Activity, Long> {
    @Query("SELECT activity FROM Activity activity WHERE activity.id = :id")
    fun findActivityById(@Param("id") id: Long): Activity?

    @Query("SELECT activity FROM Activity activity ORDER BY activity.startTime, activity.id")
    override fun findAll(): List<Activity>
}
