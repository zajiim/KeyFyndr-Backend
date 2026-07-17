-- ============================================================
-- V5: Add location fields to keys table for lost/found reports
-- Captures where a key was when it was reported LOST or FOUND.
-- Used by the Home Dashboard to show nearby activity markers.
-- ============================================================

ALTER TABLE keys ADD COLUMN latitude            DOUBLE PRECISION;
ALTER TABLE keys ADD COLUMN longitude           DOUBLE PRECISION;
ALTER TABLE keys ADD COLUMN last_status_update_at TIMESTAMP WITH TIME ZONE;

-- Index for nearby queries: find LOST/FOUND keys that have location data
CREATE INDEX idx_keys_status_location ON keys(status, latitude, longitude)
    WHERE latitude IS NOT NULL AND longitude IS NOT NULL;
