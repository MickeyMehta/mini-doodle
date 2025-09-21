CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE calendars (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    timezone VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_calendar_user_id ON calendars(user_id);
CREATE INDEX idx_calendar_created_at ON calendars(created_at);
CREATE UNIQUE INDEX idx_calendar_user_name ON calendars(user_id, name);