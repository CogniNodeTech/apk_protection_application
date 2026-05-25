const { query } = require('../db/pool');
const { hashResetToken } = require('../lib/cryptoUtil');

const DEFAULT_TTL_MS = 60 * 60 * 1000; // 1 hour

/**
 * Invalidate previous tokens and store a new hashed reset token.
 * @param {string} userId
 * @param {string} rawToken
 * @param {number} [ttlMs]
 */
async function createToken(userId, rawToken, ttlMs = DEFAULT_TTL_MS) {
  const tokenHash = hashResetToken(rawToken);
  const expiresAt = new Date(Date.now() + ttlMs);

  await query(`DELETE FROM password_reset_tokens WHERE user_id = $1 AND used_at IS NULL`, [userId]);

  const { rows } = await query(
    `INSERT INTO password_reset_tokens (user_id, token_hash, expires_at)
     VALUES ($1, $2, $3)
     RETURNING id, expires_at`,
    [userId, tokenHash, expiresAt.toISOString()]
  );
  return rows[0];
}

/**
 * @param {string} rawToken
 */
async function findActiveByRawToken(rawToken) {
  const tokenHash = hashResetToken(rawToken);
  const { rows } = await query(
    `SELECT id, user_id, expires_at, used_at
     FROM password_reset_tokens
     WHERE token_hash = $1
     ORDER BY created_at DESC
     LIMIT 1`,
    [tokenHash]
  );
  return rows[0] || null;
}

/** @param {string} tokenRowId */
async function markUsed(tokenRowId) {
  await query(
    `UPDATE password_reset_tokens SET used_at = NOW() WHERE id = $1 AND used_at IS NULL`,
    [tokenRowId]
  );
}

module.exports = {
  createToken,
  findActiveByRawToken,
  markUsed,
};
