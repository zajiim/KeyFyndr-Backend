-- ============================================================
-- V4: Add delivery timestamps to chat_messages
--
-- Replaces the boolean-only is_read column with proper timestamps
-- so the frontend can distinguish SENT, DELIVERED, and READ states.
--
-- delivered_at — set when the receiver's device acknowledges receipt
-- read_at      — set when the user opens and reads the conversation
--
-- Backfill: existing rows where is_read = TRUE are treated as
-- fully read (so delivered_at and read_at both get set to created_at
-- as a conservative estimate).
-- ============================================================

ALTER TABLE chat_messages
    ADD COLUMN delivered_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN read_at      TIMESTAMP WITH TIME ZONE;

-- Backfill: messages already marked as read get both timestamps
UPDATE chat_messages
SET delivered_at = created_at,
    read_at      = created_at
WHERE is_read = TRUE;

-- Efficient lookup for the delivery flow (find undelivered messages for a receiver)
CREATE INDEX idx_chat_messages_receiver_undelivered
    ON chat_messages(receiver_id, delivered_at)
    WHERE delivered_at IS NULL;
