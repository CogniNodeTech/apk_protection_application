const crypto = require('crypto');

function pepper() {
  return process.env.OTP_PEPPER || 'dev-only-insecure-pepper';
}

/** @param {string} code */
function hashOtpCode(code) {
  return crypto.createHash('sha256').update(`${pepper()}:otp:${code}`, 'utf8').digest('hex');
}

/** @param {string} rawToken */
function hashResetToken(rawToken) {
  return crypto.createHash('sha256').update(`${pepper()}:reset:${rawToken}`, 'utf8').digest('hex');
}

module.exports = {
  hashOtpCode,
  hashResetToken,
};
