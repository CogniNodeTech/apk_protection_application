-- =============================================================================
-- SafeGuard Consolidated Database Schemas
-- Consolidates Server Backend (MySQL), Client-Side (SQLite), and Migrations (PostgreSQL)
-- =============================================================================

-- =============================================================================
-- SECTION 1: SafeGuard Backend Database Schema (MySQL Dialect)
-- Matches the SQLAlchemy models declared in server/auth_routes.py
-- =============================================================================

-- 1. Users Table
CREATE TABLE IF NOT EXISTS `users` (
    `id` VARCHAR(50) NOT NULL,
    `fullName` VARCHAR(100) NOT NULL,
    `email` VARCHAR(150) NOT NULL,
    `phone` VARCHAR(50) NOT NULL,
    `salt_hex` VARCHAR(64) NOT NULL,
    `password_hash` VARCHAR(128) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_users_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. OTP Code Table (Dynamic short-lived codes)
CREATE TABLE IF NOT EXISTS `otps` (
    `phone` VARCHAR(50) NOT NULL,
    `code` VARCHAR(20) NOT NULL,
    `expires_at` DOUBLE NOT NULL,
    PRIMARY KEY (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Password Reset Tokens Table
CREATE TABLE IF NOT EXISTS `password_resets` (
    `token` VARCHAR(100) NOT NULL,
    `email` VARCHAR(150) NOT NULL,
    `expires_at` DOUBLE NOT NULL,
    PRIMARY KEY (`token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- =============================================================================
-- SECTION 2: Client-Side Local Database Schema (SQLite / SQLCipher Dialect)
-- Generated from Room Entity models in :data module (com.safeguard.data.local.database.entity)
-- =============================================================================

-- 1. Quarantine Records Table
CREATE TABLE IF NOT EXISTS `quarantine` (
    `id` TEXT NOT NULL,
    `originalPath` TEXT NOT NULL,
    `quarantinePath` TEXT NOT NULL,
    `apkHash` TEXT NOT NULL,
    `threatName` TEXT,
    `apkName` TEXT,
    `riskScore` INTEGER NOT NULL DEFAULT 0,
    `quarantineTimestamp` INTEGER NOT NULL,
    `autoDeleteAt` INTEGER NOT NULL,
    `sizeBytes` INTEGER NOT NULL,
    PRIMARY KEY(`id`)
);

-- 2. Trusted Applications Table
CREATE TABLE IF NOT EXISTS `trusted_apps` (
    `id` TEXT NOT NULL,
    `sha256` TEXT NOT NULL,
    `packageName` TEXT NOT NULL,
    `addedAt` INTEGER NOT NULL,
    `expiresAt` INTEGER NOT NULL,
    PRIMARY KEY(`id`)
);
CREATE INDEX IF NOT EXISTS `index_trusted_apps_sha256` ON `trusted_apps` (`sha256`);

-- 3. Scan History Table
CREATE TABLE IF NOT EXISTS `scan_history` (
    `id` TEXT NOT NULL,
    `apkHash` TEXT NOT NULL,
    `apkName` TEXT NOT NULL,
    `apkPath` TEXT NOT NULL,
    `scanTimestamp` INTEGER NOT NULL,
    `finalVerdict` TEXT NOT NULL,
    `riskScore` INTEGER NOT NULL,
    `layerResultsJson` TEXT NOT NULL,
    `wasBlocked` INTEGER NOT NULL,
    PRIMARY KEY(`id`)
);

-- 4. Scan Feedback Queue Table (Upload buffer)
CREATE TABLE IF NOT EXISTS `scan_feedback_queue` (
    `id` TEXT NOT NULL,
    `createdAtMs` INTEGER NOT NULL,
    `sha256` TEXT NOT NULL,
    `verdict` TEXT NOT NULL,
    `confidence` REAL NOT NULL,
    `packageName` TEXT,
    `versionCode` INTEGER,
    `layerScoresJson` TEXT NOT NULL,
    `triggeredRulesJson` TEXT NOT NULL,
    `androidApiLevel` INTEGER NOT NULL,
    `appVersionCode` INTEGER NOT NULL,
    PRIMARY KEY(`id`)
);
CREATE INDEX IF NOT EXISTS `index_scan_feedback_queue_createdAtMs` ON `scan_feedback_queue` (`createdAtMs`);

-- 5. Local Malware Signatures Table
CREATE TABLE IF NOT EXISTS `malware_signatures` (
    `sha256` TEXT NOT NULL,
    `sha512` TEXT,
    `fuzzyHash` TEXT,
    `threatName` TEXT NOT NULL,
    `threatFamily` TEXT,
    `severity` INTEGER NOT NULL,
    `firstSeen` INTEGER,
    `source` TEXT,
    PRIMARY KEY(`sha256`)
);
CREATE INDEX IF NOT EXISTS `index_malware_signatures_fuzzyHash` ON `malware_signatures` (`fuzzyHash`);

-- 6. User Deleted APKs (Prevent Reinstallation)
CREATE TABLE IF NOT EXISTS `deleted_apks` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `apkName` TEXT NOT NULL,
    `apkSha256` TEXT,
    `originalPath` TEXT NOT NULL,
    `threatName` TEXT,
    `riskScore` INTEGER NOT NULL,
    `deletedAt` INTEGER NOT NULL
);

-- 7. Append-only Audit Logs
CREATE TABLE IF NOT EXISTS `audit_log` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `timestamp` INTEGER NOT NULL,
    `scanId` TEXT NOT NULL,
    `apkName` TEXT NOT NULL,
    `verdict` TEXT NOT NULL,
    `action` TEXT NOT NULL,
    `riskScore` INTEGER NOT NULL
);


-- =============================================================================
-- SECTION 3: Auth PostgreSQL Migrations Database Schema (PostgreSQL 12+)
-- Setup for the auth backend service migrations
-- =============================================================================

-- 1. Users Migration Table
CREATE TABLE IF NOT EXISTS users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email VARCHAR(255) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  full_name VARCHAR(100) NOT NULL,
  phone VARCHAR(20) NOT NULL,
  email_verified BOOLEAN NOT NULL DEFAULT FALSE,
  phone_verified BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE UNIQUE INDEX IF NOT EXISTS users_email_lower_unique ON users (LOWER(email::text));
CREATE UNIQUE INDEX IF NOT EXISTS users_phone_unique ON users (phone);
COMMENT ON TABLE users IS 'Registered application users; password_hash is bcrypt.';

-- 2. OTP Challenges Table
CREATE TABLE IF NOT EXISTS otp_challenges (
  phone VARCHAR(20) PRIMARY KEY,
  code_hash VARCHAR(64) NOT NULL,
  expires_at TIMESTAMPTZ NOT NULL,
  attempts INT NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_otp_challenges_expires ON otp_challenges (expires_at);
COMMENT ON TABLE otp_challenges IS 'Phone OTP verification; code stored as SHA-256 with server pepper.';

-- 3. Password Reset Tokens Table
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
