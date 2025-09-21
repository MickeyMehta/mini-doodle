CREATE TABLE meetings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(500) NOT NULL,
    description TEXT,
    time_slot_id UUID NOT NULL UNIQUE REFERENCES time_slots(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE meeting_participants (
    meeting_id UUID NOT NULL REFERENCES meetings(id) ON DELETE CASCADE,
    participant_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (meeting_id, participant_id)
);

CREATE INDEX idx_meeting_time_slot_id ON meetings(time_slot_id);
CREATE INDEX idx_meeting_created_at ON meetings(created_at);
CREATE INDEX idx_meeting_participants_participant_id ON meeting_participants(participant_id);
CREATE INDEX idx_meeting_participants_meeting_id ON meeting_participants(meeting_id);