CREATE TABLE room
(
    room_number         INTEGER  PRIMARY KEY,
    size_square_meters  INTEGER  NOT NULL,
    number_of_beds      INTEGER  NOT NULL,
    has_balcony         BOOLEAN  NOT NULL,
    last_renovated_year INTEGER  NOT NULL,

    CONSTRAINT room_number_positive CHECK (room_number > 0),
    CONSTRAINT room_size_positive CHECK (size_square_meters > 0),
    CONSTRAINT room_number_of_beds_supported CHECK (number_of_beds IN (1, 2, 4)),
    CONSTRAINT room_last_renovated_year_plausible CHECK (last_renovated_year BETWEEN 1900 AND 2100)
);
