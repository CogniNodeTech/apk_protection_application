-- SafeGuard SQLite / SQLCipher Database Schema
-- Generated from Room Entity models in :data module (com.safeguard.data.local.database.entity)

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
