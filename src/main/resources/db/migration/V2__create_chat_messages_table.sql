-- ============================================================
-- V2: Create chat_messages table for the Chat feature module
-- Stores all 1:1 chat messages between users.
-- Conversations are derived from (sender_id, receiver_id) pairs.
-- ============================================================

CREATE TABLE chat_messages (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_id    UUID         NOT NULL REFERENCES users(id),
    receiver_id  UUID         NOT NULL REFERENCES users(id),
    content      TEXT         NOT NULL,
    is_read      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Index for fetching conversation between two users (sorted by time)
-- Uses LEAST/GREATEST to normalize the pair regardless of send direction
CREATE INDEX idx_chat_messages_conversation ON chat_messages(
    LEAST(sender_id, receiver_id),
    GREATEST(sender_id, receiver_id),
    created_at DESC
);

-- Index for fetching unread messages for a user (partial index)
CREATE INDEX idx_chat_messages_receiver_unread ON chat_messages(receiver_id, is_read)
    WHERE is_read = FALSE;

-- Indexes for listing a user's conversations (latest message per partner)
CREATE INDEX idx_chat_messages_sender ON chat_messages(sender_id, created_at DESC);
CREATE INDEX idx_chat_messages_receiver ON chat_messages(receiver_id, created_at DESC);
