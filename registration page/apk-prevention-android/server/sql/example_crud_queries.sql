-- Example CRUD against apk_prevention (run in psql / pgAdmin for learning; app uses parameterized queries)

-- Create
INSERT INTO users (full_name, email, phone, password_hash)
VALUES (
  'Jane Doe',
  'jane@example.com',
  '+15551234567',
  '$2a$12$PLACEHOLDER_BCRYPT_HASH_FROM_APP'
);

-- Read
SELECT id, email, full_name, phone, email_verified, created_at
FROM users
WHERE LOWER(email) = LOWER('jane@example.com');

-- Update profile
UPDATE users
SET full_name = 'Jane D.', updated_at = NOW()
WHERE id = '00000000-0000-0000-0000-000000000000'::uuid;

-- Delete (cascades to password_reset_tokens)
DELETE FROM users WHERE id = '00000000-0000-0000-0000-000000000000'::uuid;

-- OTP (normally written by the API, not by hand)
INSERT INTO otp_challenges (phone, code_hash, expires_at, attempts)
VALUES ('+15551234567', 'sha256_hex_here', NOW() + INTERVAL '5 minutes', 0)
ON CONFLICT (phone) DO UPDATE SET
  code_hash = EXCLUDED.code_hash,
  expires_at = EXCLUDED.expires_at,
  attempts = 0;
