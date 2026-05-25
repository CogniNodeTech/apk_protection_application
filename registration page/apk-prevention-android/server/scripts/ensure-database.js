/**
 * Creates PGDATABASE if it does not exist (connects to maintenance DB "postgres").
 * Usage: node scripts/ensure-database.js
 */
require('dotenv').config({ path: require('path').join(__dirname, '..', '.env') });

const { Client } = require('pg');
const { buildPoolConfig } = require('../db/config');

function replaceDatabaseInConnectionString(urlString, newDbName) {
  const u = new URL(urlString);
  u.pathname = `/${newDbName}`;
  return u.toString();
}

async function main() {
  const targetDb = process.env.PGDATABASE || 'apk_prevention';
  if (!/^[a-z_][a-z0-9_]*$/i.test(targetDb)) {
    throw new Error('Refusing unsafe PGDATABASE for auto-create');
  }

  const base = buildPoolConfig();
  const adminConfig = base.connectionString
    ? {
        connectionString: replaceDatabaseInConnectionString(base.connectionString, 'postgres'),
        ssl: base.ssl,
        connectionTimeoutMillis: base.connectionTimeoutMillis,
      }
    : {
        host: base.host,
        port: base.port,
        user: base.user,
        password: base.password,
        database: 'postgres',
        ssl: base.ssl,
        connectionTimeoutMillis: base.connectionTimeoutMillis,
      };

  const client = new Client(adminConfig);
  await client.connect();

  const { rows } = await client.query('SELECT 1 FROM pg_database WHERE datname = $1', [targetDb]);
  if (rows.length === 0) {
    await client.query(
      `CREATE DATABASE ${targetDb} ENCODING 'UTF8' TEMPLATE template0`
    );
    console.log(`[ensure-database] Created database "${targetDb}"`);
  } else {
    console.log(`[ensure-database] Database "${targetDb}" already exists`);
  }

  await client.end();
}

main().catch((err) => {
  console.error('[ensure-database]', err.message);
  process.exit(1);
});
