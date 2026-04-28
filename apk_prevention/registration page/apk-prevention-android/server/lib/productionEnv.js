/**
 * Validates environment for NODE_ENV=production.
 * SMS OTP and strong OTP pepper are required (matches server behavior).
 * OAuth provider keys are optional — warn only if missing.
 */

function validateProductionEnv() {
  const NODE_ENV = process.env.NODE_ENV || 'development';
  const fatal = [];
  const warnings = [];

  if (NODE_ENV !== 'production') {
    return { ok: true, fatal, warnings, skipped: true };
  }

  const twilioOk =
    process.env.TWILIO_ACCOUNT_SID &&
    process.env.TWILIO_AUTH_TOKEN &&
    process.env.TWILIO_PHONE_NUMBER;
  if (!twilioOk) {
    fatal.push(
      'SMS OTP requires TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN, and TWILIO_PHONE_NUMBER'
    );
  }

  const pepper = process.env.OTP_PEPPER || '';
  if (
    !pepper ||
    pepper === 'CHANGE_ME_OTP_PEPPER_LONG_RANDOM' ||
    pepper.includes('dev_otp_pepper') ||
    pepper.length < 16
  ) {
    fatal.push(
      'Set OTP_PEPPER to a long random secret (16+ chars), not the example or dev value'
    );
  }

  if (!process.env.GOOGLE_CLIENT_ID) {
    warnings.push('GOOGLE_CLIENT_ID is unset — Google sign-in will fail until configured');
  }
  if (!process.env.FACEBOOK_APP_ID || !process.env.FACEBOOK_APP_SECRET) {
    warnings.push('FACEBOOK_APP_ID / FACEBOOK_APP_SECRET unset — Facebook sign-in will not work');
  }
  const appleOk =
    process.env.APPLE_CLIENT_ID &&
    process.env.APPLE_TEAM_ID &&
    process.env.APPLE_KEY_ID &&
    (process.env.APPLE_PRIVATE_KEY || process.env.APPLE_PRIVATE_KEY_PATH) &&
    process.env.APPLE_REDIRECT_URI;
  if (!appleOk) {
    warnings.push(
      'Apple OAuth is incomplete — set APPLE_CLIENT_ID, APPLE_TEAM_ID, APPLE_KEY_ID, APPLE_PRIVATE_KEY (or PATH), APPLE_REDIRECT_URI'
    );
  }

  const cors = process.env.CORS_ORIGIN || '';
  if (cors.includes('localhost') || cors === 'http://localhost:3000') {
    warnings.push(
      'CORS_ORIGIN still looks like dev — set to your production web/app origin if you use browser clients'
    );
  }

  const smtpOk =
    process.env.SMTP_HOST &&
    process.env.SMTP_PORT &&
    process.env.SMTP_USER &&
    process.env.SMTP_PASS &&
    process.env.SMTP_FROM &&
    process.env.PASSWORD_RESET_BASE_URL;
  if (!smtpOk) {
    warnings.push(
      'SMTP reset email config incomplete — set SMTP_HOST, SMTP_PORT, SMTP_USER, SMTP_PASS, SMTP_FROM, PASSWORD_RESET_BASE_URL'
    );
  }

  return { ok: fatal.length === 0, fatal, warnings, skipped: false };
}

function exitIfProductionInvalid() {
  const r = validateProductionEnv();
  if (r.skipped) return;

  if (r.fatal.length) {
    console.error('FATAL: Production configuration incomplete:\n');
    r.fatal.forEach((m) => console.error(`  - ${m}`));
    process.exit(1);
  }

  if (r.warnings.length) {
    console.warn('[production] Optional checks (server will start):\n');
    r.warnings.forEach((m) => console.warn(`  - ${m}`));
  }
}

module.exports = { validateProductionEnv, exitIfProductionInvalid };
