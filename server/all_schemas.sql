-- =============================================================================
-- SafeGuard Consolidated Database Schemas (MySQL 8.0+ Compatible)
-- Consolidates Server Backend, Client-Side, and Migration schemas to run on MySQL
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
-- SECTION 2: Client-Side Local Database Schema (Translated to MySQL Dialect)
-- Originally generated from Room Entity models in :data module
-- =============================================================================

-- 1. Quarantine Records Table
CREATE TABLE IF NOT EXISTS `quarantine` (
    `id` VARCHAR(255) NOT NULL,
    `originalPath` VARCHAR(512) NOT NULL,
    `quarantinePath` VARCHAR(512) NOT NULL,
    `apkHash` VARCHAR(64) NOT NULL,
    `threatName` VARCHAR(255),
    `apkName` VARCHAR(255),
    `riskScore` INTEGER NOT NULL DEFAULT 0,
    `quarantineTimestamp` BIGINT NOT NULL,
    `autoDeleteAt` BIGINT NOT NULL,
    `sizeBytes` BIGINT NOT NULL,
    PRIMARY KEY(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Trusted Applications Table
CREATE TABLE IF NOT EXISTS `trusted_apps` (
    `id` VARCHAR(255) NOT NULL,
    `sha256` VARCHAR(64) NOT NULL,
    `packageName` VARCHAR(255) NOT NULL,
    `addedAt` BIGINT NOT NULL,
    `expiresAt` BIGINT NOT NULL,
    PRIMARY KEY(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE INDEX `index_trusted_apps_sha256` ON `trusted_apps` (`sha256`);

-- 3. Scan History Table
CREATE TABLE IF NOT EXISTS `scan_history` (
    `id` VARCHAR(255) NOT NULL,
    `apkHash` VARCHAR(64) NOT NULL,
    `apkName` VARCHAR(255) NOT NULL,
    `apkPath` VARCHAR(512) NOT NULL,
    `scanTimestamp` BIGINT NOT NULL,
    `finalVerdict` VARCHAR(50) NOT NULL,
    `riskScore` INTEGER NOT NULL,
    `layerResultsJson` TEXT NOT NULL,
    `wasBlocked` TINYINT(1) NOT NULL,
    PRIMARY KEY(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Scan Feedback Queue Table (Upload buffer)
CREATE TABLE IF NOT EXISTS `scan_feedback_queue` (
    `id` VARCHAR(255) NOT NULL,
    `createdAtMs` BIGINT NOT NULL,
    `sha256` VARCHAR(64) NOT NULL,
    `verdict` VARCHAR(50) NOT NULL,
    `confidence` DOUBLE NOT NULL,
    `packageName` VARCHAR(255),
    `versionCode` INTEGER,
    `layerScoresJson` TEXT NOT NULL,
    `triggeredRulesJson` TEXT NOT NULL,
    `androidApiLevel` INTEGER NOT NULL,
    `appVersionCode` INTEGER NOT NULL,
    PRIMARY KEY(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE INDEX `index_scan_feedback_queue_createdAtMs` ON `scan_feedback_queue` (`createdAtMs`);

-- 5. Local Malware Signatures Table
CREATE TABLE IF NOT EXISTS `malware_signatures` (
    `sha256` VARCHAR(64) NOT NULL,
    `sha512` VARCHAR(128),
    `fuzzyHash` VARCHAR(100),
    `threatName` VARCHAR(255) NOT NULL,
    `threatFamily` VARCHAR(255),
    `severity` INTEGER NOT NULL,
    `firstSeen` BIGINT,
    `source` VARCHAR(50),
    PRIMARY KEY(`sha256`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE INDEX `index_malware_signatures_fuzzyHash` ON `malware_signatures` (`fuzzyHash`);

-- 6. User Deleted APKs (Prevent Reinstallation)
CREATE TABLE IF NOT EXISTS `deleted_apks` (
    `id` INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    `apkName` VARCHAR(255) NOT NULL,
    `apkSha256` VARCHAR(64),
    `originalPath` VARCHAR(512) NOT NULL,
    `threatName` VARCHAR(255),
    `riskScore` INTEGER NOT NULL,
    `deletedAt` BIGINT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Append-only Audit Logs
CREATE TABLE IF NOT EXISTS `audit_log` (
    `id` INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    `timestamp` BIGINT NOT NULL,
    `scanId` VARCHAR(255) NOT NULL,
    `apkName` VARCHAR(255) NOT NULL,
    `verdict` VARCHAR(50) NOT NULL,
    `action` VARCHAR(50) NOT NULL,
    `riskScore` INTEGER NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- =============================================================================
-- SECTION 3: Auth PostgreSQL Migrations Database Schema (Translated to MySQL)
-- Renamed tables to prevent name conflicts with Section 1
-- =============================================================================

-- 1. Users Migration Table
CREATE TABLE IF NOT EXISTS pg_users (
  id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
  email VARCHAR(255) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  full_name VARCHAR(100) NOT NULL,
  phone VARCHAR(20) NOT NULL,
  email_verified BOOLEAN NOT NULL DEFAULT FALSE,
  phone_verified BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = 'Registered application users; password_hash is bcrypt.';
CREATE UNIQUE INDEX users_email_lower_unique ON pg_users ((LOWER(email)));
CREATE UNIQUE INDEX users_phone_unique ON pg_users (phone);

-- 2. OTP Challenges Table
CREATE TABLE IF NOT EXISTS pg_otp_challenges (
  phone VARCHAR(20) PRIMARY KEY,
  code_hash VARCHAR(64) NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  attempts INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = 'Phone OTP verification; code stored as SHA-256 with server pepper.';
CREATE INDEX idx_otp_challenges_expires ON pg_otp_challenges (expires_at);

-- 3. Password Reset Tokens Table
CREATE TABLE IF NOT EXISTS pg_password_reset_tokens (
  id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
  user_id VARCHAR(36) NOT NULL,
  token_hash VARCHAR(64) NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  used_at TIMESTAMP NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES pg_users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = 'Opaque reset tokens; raw token only sent to user (e.g. email).';
CREATE INDEX idx_password_reset_user ON pg_password_reset_tokens (user_id);
CREATE INDEX idx_password_reset_expires ON pg_password_reset_tokens (expires_at);
CREATE INDEX idx_password_reset_hash ON pg_password_reset_tokens (token_hash);
