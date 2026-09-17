CREATE TABLE person_room
(
    person_id BIGINT PRIMARY KEY REFERENCES person (id) ON DELETE CASCADE,
    room_number INTEGER NOT NULL REFERENCES room (room_number)
);

CREATE INDEX person_room_room_number_idx ON person_room (room_number);
