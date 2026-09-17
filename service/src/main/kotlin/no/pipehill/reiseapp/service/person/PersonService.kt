package no.pipehill.reiseapp.service.person

import no.pipehill.reiseapp.api.dto.CreatePersonRequest
import no.pipehill.reiseapp.api.dto.PersonResponse
import no.pipehill.reiseapp.api.dto.PersonDetailsResponse
import no.pipehill.reiseapp.api.dto.RoomResponse
import no.pipehill.reiseapp.api.dto.ActivityResponse
import no.pipehill.reiseapp.service.activity.PersonActivityRepository
import no.pipehill.reiseapp.service.accommodation.PersonRoomRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PersonService(
    private val repository: PersonRepository,
    private val assignments: PersonRoomRepository,
    private val participation: PersonActivityRepository,
) {
    @Transactional
    fun add(request: CreatePersonRequest): PersonResponse =
        repository.save(request.toEntity()).toResponse()

    @Transactional(readOnly = true)
    fun findById(id: Long): PersonDetailsResponse? {
        val person = repository.findPersonById(id) ?: return null
        val room = assignments.findRoomByPersonId(id)
        return PersonDetailsResponse(
            id = checkNotNull(person.id),
            name = person.name,
            department = person.department,
            email = person.email,
            phoneNumber = person.phoneNumber,
            gender = person.gender,
            registrationDate = person.registrationDate,
            activity = participation.findActivityByPersonId(id)?.let {
                ActivityResponse(
                    id = checkNotNull(it.id),
                    title = it.title,
                    description = it.description,
                    maxParticipants = it.maxParticipants,
                    startTime = it.startTime,
                    endTime = it.endTime,
                    notes = it.notes,
                )
            },
            assignedRoom = room?.let {
                RoomResponse(
                    roomNumber = it.roomNumber,
                    sizeSquareMeters = it.sizeSquareMeters,
                    numberOfBeds = RoomResponse.NumberOfBeds.forValue(it.numberOfBeds),
                    hasBalcony = it.hasBalcony,
                    lastRenovatedYear = it.lastRenovatedYear,
                )
            },
        )
    }

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
