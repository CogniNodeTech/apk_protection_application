-- One active OTP challenge per phone (replaced on each send-otp)
CREATE TABLE IF NOT EXISTS otp_challenges (
  phone VARCHAR(20) PRIMARY KEY,
  code_hash VARCHAR(64) NOT NULL,
  expires_at TIMESTAMPTZ NOT NULL,
  attempts INT NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_otp_challenges_expires ON otp_challenges (expires_at);

COMMENT ON TABLE otp_challenges IS 'Phone OTP verification; code stored as SHA-256 with server pepper.';
