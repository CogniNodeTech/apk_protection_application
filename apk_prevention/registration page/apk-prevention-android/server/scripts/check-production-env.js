#!/usr/bin/env node
/**
 * Run before deploy: npm run check:prod
 * Exits 1 if required production variables are missing (Twilio, OTP pepper).
 * Prints warnings for optional OAuth / CORS issues.
 */

require('dotenv').config();

const { validateProductionEnv } = require('../lib/productionEnv');

process.env.NODE_ENV = process.env.NODE_ENV || 'production';

const r = validateProductionEnv();
if (r.skipped) {
  console.log('NODE_ENV is not production — nothing to validate. Set NODE_ENV=production to check.');
  process.exit(0);
}

if (r.fatal.length) {
  console.error('Missing required production configuration:\n');
  r.fatal.forEach((m) => console.error(`  ✗ ${m}`));
  process.exit(1);
}

console.log('Required production checks: OK');

if (r.warnings.length) {
  console.warn('\nWarnings (fix if you use these features):\n');
  r.warnings.forEach((m) => console.warn(`  ⚠ ${m}`));
}

process.exit(0);
