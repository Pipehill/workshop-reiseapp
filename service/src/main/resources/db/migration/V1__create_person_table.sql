CREATE TABLE person
(
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name              VARCHAR(200) NOT NULL,
    department        VARCHAR(100) NOT NULL,
    email             VARCHAR(254) NOT NULL,
    phone_number      VARCHAR(32)  NOT NULL,
    gender            INTEGER      NOT NULL,
    registration_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT person_name_not_blank CHECK (btrim(name) <> ''),
    CONSTRAINT person_department_not_blank CHECK (btrim(department) <> ''),
    CONSTRAINT person_email_not_blank CHECK (btrim(email) <> ''),
    CONSTRAINT person_phone_number_not_blank CHECK (btrim(phone_number) <> ''),
    CONSTRAINT person_gender_supported CHECK (gender IN (0, 1, 2))
);

CREATE UNIQUE INDEX person_email_unique ON person (lower(email));
