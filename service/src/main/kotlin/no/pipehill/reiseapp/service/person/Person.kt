package no.pipehill.reiseapp.service.person

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Entity
@Table(name = "person")
class Person(
    @field:Column(nullable = false, length = 200)
    var name: String,
    @field:Column(nullable = false, length = 100)
    var department: String,
    @field:Column(nullable = false, length = 254)
    var email: String,
    @field:Column(name = "phone_number", nullable = false, length = 32)
    var phoneNumber: String,
    @field:Column(nullable = false)
    var gender: Int,
    @field:Column(name = "registration_date", nullable = false, updatable = false)
    var registrationDate: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC),
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
)
