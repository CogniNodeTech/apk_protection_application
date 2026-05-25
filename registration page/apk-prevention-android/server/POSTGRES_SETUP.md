# PostgreSQL setup (APK Prevention auth server)

## 1. Verify installation on Windows

- If `psql` is not found in PowerShell, PostgreSQL is either not installed or its `bin` folder is not on your **PATH**. Default location: `C:\Program Files\PostgreSQL\<version>\bin`.
- Add that folder to PATH (System → Environment Variables), then open a new terminal and run:

```text
psql --version
```

- Ensure the **PostgreSQL** Windows service is running (Services → `postgresql-x64-...` → Start).

## 2. Create the application database

**Option A (automated, uses `.env` credentials):** from the `server` folder run:

```text
npm run init:db
```

This connects to the `postgres` maintenance database and creates `PGDATABASE` (default `apk_prevention`) if it is missing.

**Option B (manual):** connect as `postgres`:

```text
psql -U postgres -h localhost -p 5432 -d postgres
```

Then:

```sql
CREATE DATABASE apk_prevention
  ENCODING 'UTF8'
  TEMPLATE template0;
\q
```

**Option C:** run `scripts/create-database.sql` with `psql` as in the comment inside that file.

## 3. Configure the server

1. Copy `.env.example` to `.env` (the repo’s `.env` is gitignored).
2. Set `PGPASSWORD` and other variables. Local template:

```env
PGHOST=localhost
PGPORT=5432
PGUSER=postgres
PGPASSWORD=your_password
PGDATABASE=apk_prevention
PGSSL=false
JWT_SECRET=use_a_long_random_string
OTP_PEPPER=another_long_random_secret
```

3. **Never commit `.env`.** Rotate passwords if they were ever shared.

## 4. Install dependencies and run

```text
cd server
npm install
node scripts/test-db.js
npm start
```

On startup, the server connects to PostgreSQL, applies any pending files under `migrations/`, then listens on `PORT` (default `3001`).

## 5. Migrations

- SQL files live in `migrations/` and run in alphabetical order.
- Applied versions are recorded in `schema_migrations`.
- Run manually anytime:

```text
node db/runMigrations.js
```

## 6. Health check

```text
curl http://localhost:3001/api/health
```

Expect `"database": "connected"` when PostgreSQL is reachable.

## 7. Production notes

- Use `DATABASE_URL` with `sslmode=require` (or `PGSSL=true`) for managed Postgres.
- Use a dedicated DB user with least privilege (not `superuser`).
- Store secrets in a vault or platform env vars, not in the repo.
- Enable connection pooling at the platform layer if traffic grows; tune `PGPOOL_MAX` if needed.
