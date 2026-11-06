ALTER TABLE resource ADD COLUMN has_projector BOOLEAN;

ALTER TABLE resource
ADD CONSTRAINT chk_meeting_room_projector
CHECK (type <> 'MEETING_ROOM' OR has_projector IS NOT NULL);