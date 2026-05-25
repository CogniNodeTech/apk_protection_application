-- Password reset tokens (hashed); consumed when user completes reset flow
CREATE TABLE IF NOT EXISTS password_reset_tokens (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
  token_hash VARCHAR(64) NOT NULL,
  expires_at TIMESTAMPTZ NOT NULL,
  used_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_password_reset_user ON password_reset_tokens (user_id);
CREATE INDEX IF NOT EXISTS idx_password_reset_expires ON password_reset_tokens (expires_at);
CREATE INDEX IF NOT EXISTS idx_password_reset_hash ON password_reset_tokens (token_hash);

COMMENT ON TABLE password_reset_tokens IS 'Opaque reset tokens; raw token only sent to user (e.g. email).';
