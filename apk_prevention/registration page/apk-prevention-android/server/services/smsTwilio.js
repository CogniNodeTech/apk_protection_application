/**
 * Sends OTP via Twilio SMS when TWILIO_* env vars are set.
 * In production, Twilio must be configured — no SMS is simulated.
 */

function normalizeE164(phone, defaultCountryCode = process.env.SMS_DEFAULT_COUNTRY_CODE || '91') {
  let p = String(phone).trim().replace(/[\s-]/g, '');
  if (p.startsWith('+')) return p;
  // Assume local number; prepend default country (e.g. India 91) if no leading 0
  if (p.startsWith('0')) p = p.slice(1);
  return `+${defaultCountryCode}${p}`;
}

/**
 * @returns {Promise<boolean>} true if SMS was sent
 */
async function sendOtpSms(phone, otp) {
  const sid = process.env.TWILIO_ACCOUNT_SID;
  const token = process.env.TWILIO_AUTH_TOKEN;
  const from = process.env.TWILIO_PHONE_NUMBER;

  if (!sid || !token || !from) {
    return false;
  }

  const twilio = require('twilio')(sid, token);
  const to = normalizeE164(phone);
  const body =
    process.env.TWILIO_SMS_BODY_TEMPLATE?.replace('{code}', otp) ||
    `Your APK Prevention verification code is: ${otp}. It expires in 5 minutes.`;

  await twilio.messages.create({
    body,
    from,
    to,
  });
  return true;
}

module.exports = { sendOtpSms, normalizeE164 };
