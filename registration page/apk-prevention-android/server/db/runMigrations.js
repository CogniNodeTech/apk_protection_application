const fs = require('fs');
const path = require('path');
const { pool } = require('./pool');

const MIGRATIONS_DIR = path.join(__dirname, '..', 'migrations');

async function ensureMigrationsTable(client) {
  await client.query(`
    CREATE TABLE IF NOT EXISTS schema_migrations (
      id SERIAL PRIMARY KEY,
      version VARCHAR(512) NOT NULL UNIQUE,
      applied_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );
  `);
}

/**
 * Apply pending SQL migrations in lexical order.
 * @param {import('pg').Pool} [pgPool]
 */
async function runMigrations(pgPool = pool) {
  const client = await pgPool.connect();
  try {
    await ensureMigrationsTable(client);

    const files = fs
      .readdirSync(MIGRATIONS_DIR)
      .filter((f) => f.endsWith('.sql'))
      .sort();

    for (const file of files) {
      const { rows } = await client.query('SELECT 1 FROM schema_migrations WHERE version = $1', [
        file,
      ]);
      if (rows.length > 0) continue;

      const fullPath = path.join(MIGRATIONS_DIR, file);
      const sql = fs.readFileSync(fullPath, 'utf8');

      await client.query('BEGIN');
      try {
        await client.query(sql);
        await client.query('INSERT INTO schema_migrations (version) VALUES ($1)', [file]);
        await client.query('COMMIT');
        console.log(`[migrate] applied ${file}`);
      } catch (err) {
        await client.query('ROLLBACK');
        err.message = `[migrate] ${file}: ${err.message}`;
        throw err;
      }
    }
  } finally {
    client.release();
  }
}

module.exports = { runMigrations };

if (require.main === module) {
  require('dotenv').config();
  runMigrations()
    .then(() => {
      console.log('[migrate] done');
      return pool.end();
    })
    .catch((err) => {
      console.error(err);
      pool.end().finally(() => process.exit(1));
    });
}
