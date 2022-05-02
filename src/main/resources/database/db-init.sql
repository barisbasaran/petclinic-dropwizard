CREATE TABLE IF NOT EXISTS vet
(
    id   SERIAL,
    name TEXT NOT NULL
);
ALTER TABLE vet
    ADD UNIQUE (name);

CREATE TABLE IF NOT EXISTS vet_specialty
(
    vet_id    INTEGER,
    specialty TEXT,
    PRIMARY KEY (vet_id, specialty)
);


CREATE TABLE IF NOT EXISTS pet
(
    id      SERIAL,
    name    TEXT    NOT NULL,
    age     INTEGER NOT NULL,
    species TEXT    NOT NULL
);
ALTER TABLE pet
    ADD UNIQUE (name);

CREATE TABLE IF NOT EXISTS visit
(
    id        SERIAL,
    pet_id    INTEGER   NOT NULL,
    vet_id    INTEGER   NOT NULL,
    date      TIMESTAMP NOT NULL,
    treatment TEXT      NOT NULL
);