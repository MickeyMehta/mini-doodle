CREATE TABLE time_slots (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    calendar_id UUID NOT NULL REFERENCES calendars(id) ON DELETE CASCADE,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('AVAILABLE', 'BUSY', 'BLOCKED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT check_time_slot_duration CHECK (end_time > start_time)
);

CREATE INDEX idx_time_slot_calendar_id ON time_slots(calendar_id);
CREATE INDEX idx_time_slot_start_time ON time_slots(start_time);
CREATE INDEX idx_time_slot_end_time ON time_slots(end_time);
CREATE INDEX idx_time_slot_status ON time_slots(status);
CREATE INDEX idx_time_slot_calendar_time ON time_slots(calendar_id, start_time, end_time);
CREATE INDEX idx_time_slot_time_range ON time_slots(start_time, end_time);