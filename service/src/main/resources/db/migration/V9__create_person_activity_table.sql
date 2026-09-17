CREATE TABLE person_activity
(
    person_id BIGINT PRIMARY KEY REFERENCES person(id) ON DELETE CASCADE,
    activity_id BIGINT NOT NULL REFERENCES activity(id)
);

CREATE INDEX person_activity_activity_id_idx ON person_activity(activity_id);
