/**
 * Verifies PostgreSQL connectivity, runs migrations, performs a CRUD smoke test, and cleans up.
 * Usage: node scripts/test-db.js
 */
require('dotenv').config({ path: require('path').join(__dirname, '..', '.env') });

const bcrypt = require('bcryptjs');
const { pool, query } = require('../db/pool');
const { runMigrations } = require('../db/runMigrations');

async function main() {
  console.log('[test-db] Connecting...');
  await pool.query('SELECT current_database() AS db, NOW() AS now');
  console.log('[test-db] SELECT 1 OK');

  await runMigrations();
  console.log('[test-db] Migrations OK');

  const suffix = `${Date.now()}`;
  const email = `crud_test_${suffix}@example.com`;
  const phone = `+1555${suffix.slice(-10)}`.slice(0, 20);
  const passwordHash = await bcrypt.hash('TestPass1a', 4);

  const insert = await query(
    `INSERT INTO users (full_name, email, phone, password_hash)
     VALUES ($1, $2, $3, $4)
     RETURNING id, email, full_name`,
    ['CRUD Test User', email, phone, passwordHash]
  );
  const id = insert.rows[0].id;
  console.log('[test-db] INSERT user', id);

  const sel = await query(`SELECT id, email, phone FROM users WHERE id = $1`, [id]);
  if (sel.rows.length !== 1) throw new Error('SELECT after INSERT failed');
  console.log('[test-db] SELECT OK');

  await query(`UPDATE users SET full_name = $1, updated_at = NOW() WHERE id = $2`, [
    'CRUD Test User Updated',
    id,
  ]);
  const upd = await query(`SELECT full_name FROM users WHERE id = $1`, [id]);
  if (upd.rows[0].full_name !== 'CRUD Test User Updated') throw new Error('UPDATE failed');
  console.log('[test-db] UPDATE OK');

  await query(`DELETE FROM users WHERE id = $1`, [id]);
  const del = await query(`SELECT 1 FROM users WHERE id = $1`, [id]);
  if (del.rows.length !== 0) throw new Error('DELETE failed');
  console.log('[test-db] DELETE OK');

  console.log('\n[test-db] All checks passed.\n');
}

main()
  .catch((err) => {
    console.error('[test-db] FAILED:', err.message);
    process.exitCode = 1;
  })
  .finally(() => pool.end());
