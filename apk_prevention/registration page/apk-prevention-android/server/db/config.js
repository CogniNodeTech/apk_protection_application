/**
 * PostgreSQL client configuration from environment.
 * Prefer DATABASE_URL in production; use discrete PG* vars for local dev.
 */

function buildPoolConfig() {
  const sslEnabled = String(process.env.PGSSL || '').toLowerCase() === 'true';
  const ssl =
    sslEnabled
      ? {
          rejectUnauthorized:
            String(process.env.PGSSL_REJECT_UNAUTHORIZED || 'true').toLowerCase() !== 'false',
        }
      : false;

  const base = {
    max: Number(process.env.PGPOOL_MAX || 20),
    idleTimeoutMillis: Number(process.env.PG_IDLE_MS || 30000),
    connectionTimeoutMillis: Number(process.env.PG_CONNECT_TIMEOUT_MS || 10000),
    ssl,
  };

  if (process.env.DATABASE_URL) {
    return {
      ...base,
      connectionString: process.env.DATABASE_URL,
    };
  }

  return {
    ...base,
    host: process.env.PGHOST || 'localhost',
    port: parseInt(process.env.PGPORT || '5432', 10),
    user: process.env.PGUSER || 'postgres',
    password: process.env.PGPASSWORD || '',
    database: process.env.PGDATABASE || 'apk_prevention',
  };
}

module.exports = { buildPoolConfig };
