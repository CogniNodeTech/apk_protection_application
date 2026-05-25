-- Run while connected to the default "postgres" database, e.g.:
--   psql -U postgres -h localhost -p 5432 -f scripts/create-database.sql
-- Or execute in pgAdmin Query Tool.

CREATE DATABASE apk_prevention
  WITH OWNER = CURRENT_USER
  ENCODING = 'UTF8'
  TEMPLATE = template0;
