const { query } = require('../db/pool');

const USER_SELECT = `id, email, password_hash, full_name, phone, email_verified, phone_verified,
  oauth_provider, oauth_sub, created_at, updated_at`;

/** @param {string} email */
async function findByEmail(email) {
  const { rows } = await query(
    `SELECT ${USER_SELECT}
     FROM users
     WHERE LOWER(email) = LOWER($1)
     LIMIT 1`,
    [email.trim()]
  );
  return rows[0] || null;
}

/** @param {string} provider */
/** @param {string} sub */
async function findByOAuth(provider, sub) {
  const { rows } = await query(
    `SELECT ${USER_SELECT}
     FROM users
     WHERE oauth_provider = $1 AND oauth_sub = $2
     LIMIT 1`,
    [provider, sub]
  );
  return rows[0] || null;
}

/** @param {string} phone */
async function findByPhone(phone) {
  const { rows } = await query(
    `SELECT id, email, full_name, phone FROM users WHERE phone = $1 LIMIT 1`,
    [phone.trim()]
  );
  return rows[0] || null;
}

/** @param {string} id */
async function findById(id) {
  const { rows } = await query(
    `SELECT ${USER_SELECT}
     FROM users
     WHERE id = $1`,
    [id]
  );
  return rows[0] || null;
}

/**
 * @param {{ fullName: string, email: string, phone: string, passwordHash: string }} p
 */
async function createUser({ fullName, email, phone, passwordHash }) {
  const { rows } = await query(
    `INSERT INTO users (full_name, email, phone, password_hash)
     VALUES ($1, $2, $3, $4)
     RETURNING id, email, full_name, phone, email_verified, phone_verified, created_at`,
    [fullName, email.trim(), phone.trim(), passwordHash]
  );
  return rows[0];
}

/**
 * @param {{ fullName: string, email: string, provider: string, sub: string, phone?: string|null }} p
 */
async function createOAuthUser({ fullName, email, provider, sub, phone = null }) {
  const { rows } = await query(
    `INSERT INTO users (full_name, email, phone, password_hash, oauth_provider, oauth_sub, email_verified)
     VALUES ($1, LOWER(TRIM($2::text)), $3, NULL, $4, $5, TRUE)
     RETURNING id, email, full_name, phone, email_verified, phone_verified, created_at`,
    [fullName, email, phone, provider, sub]
  );
  return rows[0];
}

/** @param {string} id */
async function setPhoneVerified(id) {
  await query(`UPDATE users SET phone_verified = TRUE, updated_at = NOW() WHERE id = $1`, [id]);
}

/**
 * @param {string} userId
 * @param {string} passwordHash
 */
async function updatePassword(userId, passwordHash) {
  await query(
    `UPDATE users
     SET password_hash = $2,
         oauth_provider = NULL,
         oauth_sub = NULL,
         updated_at = NOW()
     WHERE id = $1`,
    [userId, passwordHash]
  );
}

module.exports = {
  findByEmail,
  findByOAuth,
  findByPhone,
  findById,
  createUser,
  createOAuthUser,
  setPhoneVerified,
  updatePassword,
};
