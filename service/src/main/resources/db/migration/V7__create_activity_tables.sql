CREATE TABLE activity
(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title VARCHAR(200) NOT NULL CHECK (btrim(title) <> ''),
    description TEXT NOT NULL CHECK (btrim(description) <> ''),
    max_participants INTEGER NOT NULL CHECK (max_participants > 0),
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    notes TEXT NOT NULL DEFAULT '',
    CONSTRAINT activity_duration CHECK (
        end_time - start_time BETWEEN INTERVAL '2 hours' AND INTERVAL '8 hours'
    )
);
