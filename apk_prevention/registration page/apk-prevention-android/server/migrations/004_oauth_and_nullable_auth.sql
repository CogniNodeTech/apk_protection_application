-- OAuth sign-in + nullable password/phone for provider-only accounts
ALTER TABLE users ALTER COLUMN password_hash DROP NOT NULL;
ALTER TABLE users ALTER COLUMN phone DROP NOT NULL;

ALTER TABLE users ADD COLUMN IF NOT EXISTS oauth_provider VARCHAR(32) NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS oauth_sub VARCHAR(255) NULL;

-- At most one of: password login OR OAuth (enforced in application; DB allows consistency)
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_auth_method_check;
ALTER TABLE users ADD CONSTRAINT users_auth_method_check CHECK (
  password_hash IS NOT NULL OR (oauth_provider IS NOT NULL AND oauth_sub IS NOT NULL)
);

-- Phone unique only when set (multiple OAuth users may add phone later)
DROP INDEX IF EXISTS users_phone_unique;
CREATE UNIQUE INDEX IF NOT EXISTS users_phone_unique ON users (phone) WHERE phone IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS users_oauth_provider_sub_unique
  ON users (oauth_provider, oauth_sub)
  WHERE oauth_provider IS NOT NULL AND oauth_sub IS NOT NULL;

COMMENT ON COLUMN users.oauth_provider IS 'google | facebook | apple';
COMMENT ON COLUMN users.oauth_sub IS 'Stable subject from the identity provider';
