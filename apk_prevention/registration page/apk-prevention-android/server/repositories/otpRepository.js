const { query } = require('../db/pool');
const { hashOtpCode } = require('../lib/cryptoUtil');

/**
 * @param {string} phone
 * @param {string} code
 * @param {number} ttlMs
 */
async function upsertChallenge(phone, code, ttlMs) {
  const codeHash = hashOtpCode(code);
  const expiresAt = new Date(Date.now() + ttlMs);
  await query(
    `INSERT INTO otp_challenges (phone, code_hash, expires_at, attempts)
     VALUES ($1, $2, $3, 0)
     ON CONFLICT (phone) DO UPDATE SET
       code_hash = EXCLUDED.code_hash,
       expires_at = EXCLUDED.expires_at,
       attempts = 0,
       created_at = NOW()`,
    [phone.trim(), codeHash, expiresAt.toISOString()]
  );
}

/** @param {string} phone */
async function getChallenge(phone) {
  const { rows } = await query(
    `SELECT phone, code_hash, expires_at, attempts
     FROM otp_challenges
     WHERE phone = $1`,
    [phone.trim()]
  );
  return rows[0] || null;
}

/** @param {string} phone */
async function incrementAttempts(phone) {
  await query(
    `UPDATE otp_challenges SET attempts = attempts + 1 WHERE phone = $1`,
    [phone.trim()]
  );
}

/** @param {string} phone */
async function deleteChallenge(phone) {
  await query(`DELETE FROM otp_challenges WHERE phone = $1`, [phone.trim()]);
}

/** @param {string} code */
function verifyCode(storedHash, code) {
  return storedHash === hashOtpCode(code);
}

module.exports = {
  upsertChallenge,
  getChallenge,
  incrementAttempts,
  deleteChallenge,
  verifyCode,
};
