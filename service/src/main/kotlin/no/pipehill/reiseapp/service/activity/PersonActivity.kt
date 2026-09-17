package no.pipehill.reiseapp.service.activity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "person_activity")
class PersonActivity(
    @field:Id
    @field:Column(name = "person_id", nullable = false)
    var personId: Long,
    @field:Column(name = "activity_id", nullable = false)
    var activityId: Long,
)
