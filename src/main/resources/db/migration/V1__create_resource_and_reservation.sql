CREATE TABLE resource (
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    type      VARCHAR(20)  NOT NULL,
    name      VARCHAR(100) NOT NULL,
    capacity  INT          NOT NULL,

    CONSTRAINT chk_resource_type     CHECK (type IN ('MEETING_ROOM', 'DESK_SPACE')),
    CONSTRAINT chk_resource_capacity CHECK (capacity > 0)
);

CREATE TABLE reservation (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    resource_id BIGINT    NOT NULL REFERENCES resource (id),
    start_time  TIMESTAMP NOT NULL,
    end_time    TIMESTAMP NOT NULL,

    CONSTRAINT chk_reservation_time CHECK (start_time < end_time)
);

CREATE INDEX idx_reservation_resource_id ON reservation (resource_id);

CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE reservation
ADD CONSTRAINT no_overlapping_reservation
EXCLUDE USING gist (
    resource_id WITH =,
    tsrange(start_time, end_time) WITH &&
);