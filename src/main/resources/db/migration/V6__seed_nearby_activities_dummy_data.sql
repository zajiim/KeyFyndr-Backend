-- ============================================================
-- V6: Seed dummy nearby activities (LOST/FOUND keys with location)
-- Used to test the Home Dashboard GET /api/v1/home?latitude=&longitude=
-- recentNearbyActivities feed.
--
-- All dummy keys are assigned to a placeholder owner UUID.
-- They are placed around Bangalore (12.97°N, 77.59°E) to match
-- a realistic test lat/lng.
-- ============================================================

-- Create a dummy owner user if one doesn't already exist
-- (used only to satisfy the FK constraint on keys.owner_id)
INSERT INTO users (id, name, email, phone, password_hash, verified, created_at)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'Seed Data User',
    'seed@keyfyndr.internal',
    '+919999999999',
    '$2a$10$dummyhashforseeddataonlyXXXXXXXXXXXXXXXXXXXXXXXXXX',
    TRUE,
    NOW()
)
ON CONFLICT (id) DO NOTHING;

-- ─── Dummy LOST keys (with GPS coordinates near Bangalore) ──────────────────

INSERT INTO keys (id, public_key_id, title, description, color, category, image_url, status, is_active, owner_id, latitude, longitude, last_status_update_at, created_at)
VALUES
    -- ~500m north of 12.9716, 77.5946 (Koramangala area)
    (gen_random_uuid(), 'SEED000001', 'Silver House Keys',
     'Lost near the apartment entrance', 'Silver', 'HOME',
     NULL, 'LOST', TRUE, '00000000-0000-0000-0000-000000000001',
     12.9761, 77.5946, NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours'),

    -- ~1.2km south-west
    (gen_random_uuid(), 'SEED000002', 'Car Key (Honda City)',
     'Black remote key fob, 2 buttons', 'Black', 'VEHICLE',
     NULL, 'LOST', TRUE, '00000000-0000-0000-0000-000000000001',
     12.9620, 77.5862, NOW() - INTERVAL '5 hours', NOW() - INTERVAL '5 hours'),

    -- ~800m east
    (gen_random_uuid(), 'SEED000003', 'Office Keycard + Keys',
     'Blue lanyard, attached to 3 keys', 'Blue', 'OFFICE',
     NULL, 'LOST', TRUE, '00000000-0000-0000-0000-000000000001',
     12.9716, 77.6018, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),

    -- ~2km north
    (gen_random_uuid(), 'SEED000004', 'Bike Key (Royal Enfield)',
     'Red key with a small scratch', 'Red', 'VEHICLE',
     NULL, 'LOST', TRUE, '00000000-0000-0000-0000-000000000001',
     12.9896, 77.5946, NOW() - INTERVAL '3 hours', NOW() - INTERVAL '3 hours'),

    -- ~300m south-east
    (gen_random_uuid(), 'SEED000005', 'Gym Locker Key',
     'Yellow tagged, number 42', 'Yellow', 'MISC',
     NULL, 'LOST', TRUE, '00000000-0000-0000-0000-000000000001',
     12.9689, 77.5973, NOW() - INTERVAL '30 minutes', NOW() - INTERVAL '30 minutes');

-- ─── Dummy FOUND keys (with GPS coordinates near Bangalore) ─────────────────

INSERT INTO keys (id, public_key_id, title, description, color, category, image_url, status, is_active, owner_id, latitude, longitude, last_status_update_at, created_at)
VALUES
    -- ~600m south
    (gen_random_uuid(), 'SEED000006', 'Found: Bunch of House Keys',
     'Found near the bus stop on 80ft road', 'Silver', 'HOME',
     NULL, 'FOUND', TRUE, '00000000-0000-0000-0000-000000000001',
     12.9656, 77.5946, NOW() - INTERVAL '1 hour', NOW() - INTERVAL '1 hour'),

    -- ~1.5km north-east
    (gen_random_uuid(), 'SEED000007', 'Found: Toyota Car Key',
     'Black remote, found in parking lot', 'Black', 'VEHICLE',
     NULL, 'FOUND', TRUE, '00000000-0000-0000-0000-000000000001',
     12.9851, 77.6080, NOW() - INTERVAL '4 hours', NOW() - INTERVAL '4 hours'),

    -- ~400m west
    (gen_random_uuid(), 'SEED000008', 'Found: Office Badge + Key',
     'Company ID card, ABC Corp', 'White', 'OFFICE',
     NULL, 'FOUND', TRUE, '00000000-0000-0000-0000-000000000001',
     12.9716, 77.5906, NOW() - INTERVAL '6 hours', NOW() - INTERVAL '6 hours'),

    -- ~1km south-west
    (gen_random_uuid(), 'SEED000009', 'Found: Scooter Key',
     'Honda Activa key with Ganesha charm', 'Gold', 'VEHICLE',
     NULL, 'FOUND', TRUE, '00000000-0000-0000-0000-000000000001',
     12.9626, 77.5876, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),

    -- ~200m north-west
    (gen_random_uuid(), 'SEED000010', 'Found: Padlock Key',
     'Small brass key with red tag', 'Brass', 'MISC',
     NULL, 'FOUND', TRUE, '00000000-0000-0000-0000-000000000001',
     12.9734, 77.5928, NOW() - INTERVAL '15 minutes', NOW() - INTERVAL '15 minutes');
