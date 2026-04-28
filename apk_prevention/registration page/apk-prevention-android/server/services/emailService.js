/**
 * SMTP mail delivery helper for password reset links.
 * Works with any SMTP provider (Gmail, SendGrid SMTP, Mailgun SMTP, etc.).
 */
const nodemailer = require('nodemailer');

function getMailerConfig() {
  const host = process.env.SMTP_HOST;
  const port = Number(process.env.SMTP_PORT || 587);
  const user = process.env.SMTP_USER;
  const pass = process.env.SMTP_PASS;
  const from = process.env.SMTP_FROM;

  if (!host || !port || !user || !pass || !from) {
    return null;
  }

  return {
    host,
    port,
    secure: port === 465,
    auth: { user, pass },
    from,
  };
}

function buildResetUrl(rawToken) {
  const baseUrl = process.env.PASSWORD_RESET_BASE_URL;
  if (!baseUrl) return null;
  const sep = baseUrl.includes('?') ? '&' : '?';
  return `${baseUrl}${sep}token=${encodeURIComponent(rawToken)}`;
}

/**
 * @returns {Promise<{sent:boolean, resetUrl?:string}>}
 */
async function sendPasswordResetEmail(toEmail, rawToken) {
  const cfg = getMailerConfig();
  const resetUrl = buildResetUrl(rawToken);

  if (!cfg || !resetUrl) {
    return { sent: false, resetUrl };
  }

  const transporter = nodemailer.createTransport({
    host: cfg.host,
    port: cfg.port,
    secure: cfg.secure,
    auth: cfg.auth,
  });

  const appName = process.env.APP_NAME || 'APK Prevention';
  await transporter.sendMail({
    from: cfg.from,
    to: toEmail,
    subject: `${appName} password reset`,
    text: `We received a request to reset your password.\n\nReset your password using this link:\n${resetUrl}\n\nThis link expires in 1 hour.\nIf you did not request this, ignore this email.`,
    html: `
      <p>We received a request to reset your password.</p>
      <p><a href="${resetUrl}">Reset your password</a></p>
      <p>This link expires in 1 hour.</p>
      <p>If you did not request this, ignore this email.</p>
    `,
  });

  return { sent: true, resetUrl };
}

module.exports = {
  sendPasswordResetEmail,
  buildResetUrl,
};
