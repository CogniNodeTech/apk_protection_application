const { Pool } = require('pg');
const { buildPoolConfig } = require('./config');

const pool = new Pool(buildPoolConfig());

pool.on('error', (err) => {
  console.error('[pg] Unexpected pool error', err.message);
});

/**
 * @param {string} text
 * @param {any[]} [params]
 */
async function query(text, params) {
  return pool.query(text, params);
}

async function withTransaction(fn) {
  const client = await pool.connect();
  try {
    await client.query('BEGIN');
    const result = await fn(client);
    await client.query('COMMIT');
    return result;
  } catch (e) {
    await client.query('ROLLBACK');
    throw e;
  } finally {
    client.release();
  }
}

module.exports = {
  pool,
  query,
  withTransaction,
};
