-- ============================================================
-- V1: Create keys table for the Key feature module
-- Represents physical keys tracked via unique public IDs.
-- Keys use soft-delete (is_active) rather than hard deletion.
-- ============================================================

CREATE TABLE keys (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    public_key_id VARCHAR(10)  NOT NULL UNIQUE,
    title         VARCHAR(255) NOT NULL,
    description   TEXT,
    color         VARCHAR(50),
    category      VARCHAR(100) NOT NULL,
    image_url     TEXT,
    status        VARCHAR(20)  NOT NULL DEFAULT 'SAFE',
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    owner_id      UUID         NOT NULL REFERENCES users(id),
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Index for fetching all active keys by owner (primary query pattern)
CREATE INDEX idx_keys_owner_active ON keys(owner_id, is_active);

-- Index for public key ID lookups (used by the public endpoint)
CREATE INDEX idx_keys_public_key_id ON keys(public_key_id);

-- Index for filtering by status (e.g., dashboard analytics)
CREATE INDEX idx_keys_status ON keys(status);
