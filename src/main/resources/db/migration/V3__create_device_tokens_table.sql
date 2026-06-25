-- ============================================================
-- V3: Create device_tokens table for FCM Push Notifications
-- Stores FCM registration tokens per device per user.
-- A single user may have multiple tokens (multi-device support).
-- Tokens are cleaned up on logout and replaced on re-login.
-- ============================================================

CREATE TABLE device_tokens (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    device_token TEXT        NOT NULL,
    device_type  VARCHAR(20) NOT NULL DEFAULT 'ANDROID', -- 'ANDROID' | 'IOS'
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_device_token UNIQUE (device_token)
);

-- Fast lookup for sending notifications to all user devices
CREATE INDEX idx_device_tokens_user_id ON device_tokens(user_id);
