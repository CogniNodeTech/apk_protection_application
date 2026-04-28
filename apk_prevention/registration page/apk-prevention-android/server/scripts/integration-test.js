/**
 * Starts the API briefly and exercises register, login, OTP, health, and DB persistence.
 * Usage: node scripts/integration-test.js
 */
const path = require('path');
const http = require('http');
const { spawn } = require('child_process');

require('dotenv').config({ path: path.join(__dirname, '..', '.env') });

const SERVER_ROOT = path.join(__dirname, '..');
const BASE = 'http://127.0.0.1:3001';

function httpJson(method, urlPath, body) {
  return new Promise((resolve, reject) => {
    const u = new URL(BASE + urlPath);
    const opts = {
      hostname: u.hostname,
      port: u.port,
      path: u.pathname,
      method,
      headers: { 'Content-Type': 'application/json' },
    };
    const req = http.request(opts, (res) => {
      let raw = '';
      res.on('data', (c) => {
        raw += c;
      });
      res.on('end', () => {
        let parsed = raw;
        try {
          parsed = raw ? JSON.parse(raw) : {};
        } catch {
          /* keep raw */
        }
        resolve({ status: res.statusCode, body: parsed, raw });
      });
    });
    req.on('error', reject);
    req.setTimeout(15000, () => {
      req.destroy(new Error('Request timeout'));
    });
    if (body) req.write(JSON.stringify(body));
    req.end();
  });
}

async function waitForHealth(maxMs = 20000) {
  const deadline = Date.now() + maxMs;
  while (Date.now() < deadline) {
    try {
      const r = await httpJson('GET', '/api/health');
      if (r.status === 200 && r.body.database === 'connected') return;
    } catch {
      /* retry */
    }
    await new Promise((r) => setTimeout(r, 250));
  }
  throw new Error('Server did not reach healthy state in time');
}

function assert(cond, msg) {
  if (!cond) throw new Error(msg);
}

async function main() {
  let serverOut = '';
  const proc = spawn(process.execPath, ['index.js'], {
    cwd: SERVER_ROOT,
    env: { ...process.env },
    stdio: ['ignore', 'pipe', 'pipe'],
  });

  proc.stdout.on('data', (c) => {
    serverOut += c.toString();
  });
  proc.stderr.on('data', (c) => {
    serverOut += c.toString();
  });

  proc.on('error', (err) => {
    console.error('[integration] spawn failed:', err.message);
  });

  const killServer = () => {
    try {
      proc.kill('SIGTERM');
    } catch {
      /* ignore */
    }
  };

  try {
    await waitForHealth();

    const h = await httpJson('GET', '/api/health');
    assert(h.status === 200 && h.body.status === 'ok', `Health expected 200 ok, got ${h.status} ${JSON.stringify(h.body)}`);
    assert(h.body.database === 'connected', 'Health should report database connected');
    console.log('[integration] GET /api/health OK');

    const suffix = `${Date.now()}`;
    const email = `e2e_${suffix}@example.com`;
    const phone = `+1555${suffix.slice(-10)}`;
    const password = 'TestPass1a';
    const fullName = 'E2E User';

    const reg = await httpJson('POST', '/api/auth/register', {
      fullName,
      email,
      phone,
      password,
    });
    assert(reg.status === 201 && reg.body.success === true, `Register failed: ${reg.status} ${JSON.stringify(reg.body)}`);
    assert(reg.body.token && reg.body.user && reg.body.user.email, 'Register response missing token/user');
    assert(reg.body.user.fullName === fullName, 'Register user.fullName mismatch');
    console.log('[integration] POST /api/auth/register OK');

    const dup = await httpJson('POST', '/api/auth/register', {
      fullName,
      email,
      phone,
      password,
    });
    assert(dup.status === 409, `Duplicate register should be 409, got ${dup.status}`);
    console.log('[integration] duplicate email rejected OK');

    const badLogin = await httpJson('POST', '/api/auth/login', {
      email,
      password: 'WrongPass1a',
    });
    assert(badLogin.status === 401, `Bad password should be 401, got ${badLogin.status}`);
    console.log('[integration] bad password rejected OK');

    const login = await httpJson('POST', '/api/auth/login', { email, password });
    assert(login.status === 200 && login.body.success && login.body.token, `Login failed: ${JSON.stringify(login.body)}`);
    assert(login.body.user.email === email, 'Login email mismatch');
    console.log('[integration] POST /api/auth/login OK');

    const sendOtp = await httpJson('POST', '/api/auth/send-otp', { phone });
    assert(sendOtp.status === 200 && sendOtp.body.success, `send-otp failed: ${JSON.stringify(sendOtp.body)}`);
    await new Promise((r) => setTimeout(r, 400));
    const otpMatch = serverOut.match(/\[OTP-DEV\] Code for (.+):\s*(\d{6})/);
    const code = otpMatch ? otpMatch[2] : null;
    assert(code, `Could not parse OTP from server log. Log tail:\n${serverOut.slice(-800)}`);

    const badOtp = await httpJson('POST', '/api/auth/verify-otp', { phone, code: '000000' });
    assert(badOtp.status === 400, `Bad OTP should be 400, got ${badOtp.status}`);
    console.log('[integration] verify-otp wrong code rejected OK');

    const verify = await httpJson('POST', '/api/auth/verify-otp', { phone, code });
    assert(verify.status === 200 && verify.body.success, `verify-otp failed: ${JSON.stringify(verify.body)}`);
    console.log('[integration] POST /api/auth/verify-otp OK');

    const reset = await httpJson('POST', '/api/auth/reset-password', { email });
    assert(reset.status === 200 && reset.body.success, `reset-password failed: ${JSON.stringify(reset.body)}`);
    console.log('[integration] POST /api/auth/reset-password OK');

    const { pool } = require('../db/pool');
    const row = await pool.query(
      'SELECT id, email, phone_verified FROM users WHERE LOWER(email) = LOWER($1)',
      [email]
    );
    assert(row.rows.length === 1, 'User not found in database after register');
    assert(row.rows[0].phone_verified === true, 'phone_verified should be true after OTP verify');
    await pool.end();
    console.log('[integration] PostgreSQL row check OK (phone_verified)');

    console.log('\n[integration] All API + DB checks passed.\n');
  } catch (err) {
    console.error('[integration] Server log tail:\n', serverOut.slice(-2500));
    throw err;
  } finally {
    killServer();
    await new Promise((r) => setTimeout(r, 500));
  }
}

main().catch((err) => {
  console.error('[integration] FAILED:', err.message);
  process.exitCode = 1;
});
