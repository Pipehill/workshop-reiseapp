package no.pipehill.reiseapp.service.room

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface RoomRepository : JpaRepository<Room, Int> {
    @Query("SELECT room FROM Room room WHERE room.roomNumber = :roomNumber")
    fun findRoomByNumber(@Param("roomNumber") roomNumber: Int): Room?

    @Query("SELECT room FROM Room room ORDER BY room.roomNumber")
    override fun findAll(): List<Room>
}
