require('dotenv').config();

const crypto = require('crypto');
const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');
const morgan = require('morgan');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { body, validationResult } = require('express-validator');

const { pool } = require('./db/pool');
const { runMigrations } = require('./db/runMigrations');
const userRepository = require('./repositories/userRepository');
const otpRepository = require('./repositories/otpRepository');
const passwordResetRepository = require('./repositories/passwordResetRepository');
const { sendOtpSms } = require('./services/smsTwilio');
const { sendPasswordResetEmail } = require('./services/emailService');
const {
  verifyGoogleIdToken,
  verifyFacebookAccessToken,
  verifyAppleIdentityToken,
  exchangeAppleAuthorizationCode,
} = require('./services/oauthProviders');
const { findOrCreateOAuthUser } = require('./services/oauthUserService');
const { exitIfProductionInvalid } = require('./lib/productionEnv');

const app = express();
const PORT = process.env.PORT || 3001;
const JWT_SECRET = process.env.JWT_SECRET;
const NODE_ENV = process.env.NODE_ENV || 'development';

if (!JWT_SECRET || JWT_SECRET === 'CHANGE_ME_TO_A_LONG_RANDOM_STRING_IN_PRODUCTION') {
  if (NODE_ENV === 'production') {
    console.error('FATAL: Set a strong JWT_SECRET in .env for production');
    process.exit(1);
  }
  console.warn('WARNING: Using default JWT_SECRET. Set a strong secret in .env');
}

// --- Security Middleware ---
app.use(helmet());
app.use(
  cors({
    origin: process.env.CORS_ORIGIN || 'http://localhost:3000',
    methods: ['GET', 'POST'],
    allowedHeaders: ['Content-Type', 'Authorization'],
  })
);
app.use(express.json({ limit: '10kb' }));
app.use(morgan(NODE_ENV === 'production' ? 'combined' : 'dev'));

// --- Rate Limiters ---
const authLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 15,
  standardHeaders: true,
  legacyHeaders: false,
  message: { success: false, message: 'Too many attempts. Try again later.' },
});

const otpLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 5,
  standardHeaders: true,
  legacyHeaders: false,
  message: { success: false, message: 'Too many OTP requests. Try again later.' },
});

// --- Helpers ---
function generateToken(user) {
  return jwt.sign({ id: user.id, email: user.email }, JWT_SECRET, {
    algorithm: 'HS256',
    expiresIn: '7d',
  });
}

function sanitizeUser(user) {
  return {
    id: user.id,
    fullName: user.full_name,
    email: user.email,
    phone: user.phone,
  };
}

function handleValidationErrors(req, res) {
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return res.status(400).json({
      success: false,
      message: errors.array()[0].msg,
    });
  }
  return null;
}

function mapUniqueViolation(err) {
  if (!err || err.code !== '23505') return null;
  const d = `${err.detail || ''} ${err.constraint || ''}`.toLowerCase();
  if (d.includes('email') || d.includes('users_email')) {
    return { status: 409, message: 'This email is already registered' };
  }
  if (d.includes('phone') || d.includes('users_phone')) {
    return { status: 409, message: 'This phone number is already registered' };
  }
  return { status: 409, message: 'This value is already in use' };
}

// =============================================
// AUTH ENDPOINTS
// =============================================

app.post(
  '/api/auth/register',
  authLimiter,
  [
    body('fullName')
      .trim()
      .notEmpty()
      .withMessage('Full name is required')
      .isLength({ min: 2, max: 100 })
      .withMessage('Name must be 2–100 characters'),
    body('email').trim().isEmail().normalizeEmail().withMessage('Valid email is required'),
    body('phone')
      .trim()
      .notEmpty()
      .withMessage('Phone number is required')
      .isLength({ min: 10, max: 20 })
      .withMessage('Phone must be 10–20 characters'),
    body('password')
      .isLength({ min: 8, max: 128 })
      .withMessage('Password must be 8–128 characters')
      .matches(/[A-Z]/)
      .withMessage('Password must contain an uppercase letter')
      .matches(/[a-z]/)
      .withMessage('Password must contain a lowercase letter')
      .matches(/\d/)
      .withMessage('Password must contain a number'),
  ],
  async (req, res, next) => {
    try {
      const invalid = handleValidationErrors(req, res);
      if (invalid) return;

      const { fullName, email, phone, password } = req.body;

      const existingUser = await userRepository.findByEmail(email);
      if (existingUser) {
        return res.status(409).json({
          success: false,
          message: 'This email is already registered',
        });
      }

      const hashedPassword = await bcrypt.hash(password, 12);
      let newUser;
      try {
        newUser = await userRepository.createUser({
          fullName,
          email,
          phone,
          passwordHash: hashedPassword,
        });
      } catch (err) {
        const mapped = mapUniqueViolation(err);
        if (mapped) {
          return res.status(mapped.status).json({ success: false, message: mapped.message });
        }
        throw err;
      }

      const token = generateToken(newUser);

      res.status(201).json({
        success: true,
        message: 'Registration successful',
        token,
        user: sanitizeUser(newUser),
      });
    } catch (error) {
      next(error);
    }
  }
);

app.post(
  '/api/auth/login',
  authLimiter,
  [
    body('email').trim().isEmail().normalizeEmail().withMessage('Valid email is required'),
    body('password').notEmpty().withMessage('Password is required'),
  ],
  async (req, res, next) => {
    try {
      const invalid = handleValidationErrors(req, res);
      if (invalid) return;

      const { email, password } = req.body;

      const user = await userRepository.findByEmail(email);
      if (!user) {
        return res.status(401).json({
          success: false,
          message: 'Invalid email or password',
        });
      }

      if (!user.password_hash) {
        return res.status(401).json({
          success: false,
          message: 'This account uses social sign-in. Use Google, Facebook, or Apple.',
        });
      }

      const isMatch = await bcrypt.compare(password, user.password_hash);
      if (!isMatch) {
        return res.status(401).json({
          success: false,
          message: 'Invalid email or password',
        });
      }

      const token = generateToken(user);

      res.json({
        success: true,
        message: 'Login successful',
        token,
        user: sanitizeUser(user),
      });
    } catch (error) {
      next(error);
    }
  }
);

app.post(
  '/api/auth/send-otp',
  otpLimiter,
  [
    body('phone')
      .trim()
      .notEmpty()
      .withMessage('Phone number is required')
      .isLength({ min: 10, max: 20 })
      .withMessage('Invalid phone number'),
  ],
  async (req, res, next) => {
    try {
      const invalid = handleValidationErrors(req, res);
      if (invalid) return;

      const { phone } = req.body;

      const otp = Math.floor(100000 + Math.random() * 900000).toString();
      await otpRepository.upsertChallenge(phone, otp, 5 * 60 * 1000);

      let smsSent = false;
      try {
        smsSent = await sendOtpSms(phone, otp);
      } catch (smsErr) {
        console.error('[sms]', smsErr.message);
        if (NODE_ENV === 'production') {
          return res.status(503).json({
            success: false,
            message: 'Unable to send SMS. Try again later or contact support.',
          });
        }
        throw smsErr;
      }

      if (NODE_ENV === 'production' && !smsSent) {
        return res.status(503).json({
          success: false,
          message: 'SMS delivery is not configured. Contact support.',
        });
      }

      if (NODE_ENV !== 'production') {
        console.log(`[OTP-DEV] Code for ${phone}: ${otp}`);
      }

      const payload = {
        success: true,
        message: smsSent
          ? `OTP sent to ${phone}`
          : 'OTP generated (SMS gateway not configured — use debug code in non-production)',
      };
      if (NODE_ENV !== 'production' && !smsSent) {
        payload.debugOtp = otp;
      }
      res.json(payload);
    } catch (error) {
      next(error);
    }
  }
);

app.post(
  '/api/auth/verify-otp',
  otpLimiter,
  [
    body('phone').trim().notEmpty().withMessage('Phone number is required'),
    body('code').trim().isLength({ min: 6, max: 6 }).isNumeric().withMessage('Invalid OTP format'),
  ],
  async (req, res, next) => {
    try {
      const invalid = handleValidationErrors(req, res);
      if (invalid) return;

      const { phone, code } = req.body;

      const stored = await otpRepository.getChallenge(phone);
      if (!stored) {
        return res.status(400).json({
          success: false,
          message: 'No OTP was sent to this number',
        });
      }

      if (Date.now() > new Date(stored.expires_at).getTime()) {
        await otpRepository.deleteChallenge(phone);
        return res.status(400).json({
          success: false,
          message: 'OTP has expired. Request a new one',
        });
      }

      if (!otpRepository.verifyCode(stored.code_hash, code)) {
        await otpRepository.incrementAttempts(phone);
        const updated = await otpRepository.getChallenge(phone);
        if (updated && updated.attempts > 5) {
          await otpRepository.deleteChallenge(phone);
          return res.status(429).json({
            success: false,
            message: 'Too many failed attempts. Request a new OTP',
          });
        }
        return res.status(400).json({
          success: false,
          message: 'Invalid OTP code',
        });
      }

      await otpRepository.deleteChallenge(phone);

      const user = await userRepository.findByPhone(phone);
      if (user) {
        await userRepository.setPhoneVerified(user.id);
        const fullUser = await userRepository.findById(user.id);
        const token = generateToken(fullUser);
        return res.json({
          success: true,
          message: 'Phone verified successfully',
          token,
          user: sanitizeUser(fullUser),
        });
      }

      res.json({
        success: true,
        message: 'Phone verified successfully',
      });
    } catch (error) {
      next(error);
    }
  }
);

app.post(
  '/api/auth/reset-password',
  authLimiter,
  [body('email').trim().isEmail().normalizeEmail().withMessage('Valid email is required')],
  async (req, res, next) => {
    try {
      const invalid = handleValidationErrors(req, res);
      if (invalid) return;

      const { email } = req.body;

      const user = await userRepository.findByEmail(email);
      if (user) {
        const rawToken = crypto.randomBytes(32).toString('hex');
        await passwordResetRepository.createToken(user.id, rawToken);
        const { sent, resetUrl } = await sendPasswordResetEmail(email, rawToken);

        if (!sent && NODE_ENV === 'production') {
          return res.status(503).json({
            success: false,
            message: 'Password reset email service is unavailable. Try again later.',
          });
        }

        if (NODE_ENV !== 'production') {
          if (resetUrl) {
            console.log(`[PASSWORD-RESET-DEV] email=${email} link=${resetUrl}`);
          } else {
            console.log(`[PASSWORD-RESET-DEV] email=${email} token=${rawToken}`);
          }
        }
      }

      res.json({
        success: true,
        message: 'If this email is registered, a reset link has been sent',
      });
    } catch (error) {
      next(error);
    }
  }
);

app.post(
  '/api/auth/reset-password/confirm',
  authLimiter,
  [
    body('token').trim().notEmpty().withMessage('Reset token is required'),
    body('newPassword')
      .isLength({ min: 8, max: 128 })
      .withMessage('Password must be 8–128 characters')
      .matches(/[A-Z]/)
      .withMessage('Password must contain an uppercase letter')
      .matches(/[a-z]/)
      .withMessage('Password must contain a lowercase letter')
      .matches(/\d/)
      .withMessage('Password must contain a number'),
  ],
  async (req, res, next) => {
    try {
      const invalid = handleValidationErrors(req, res);
      if (invalid) return;

      const { token, newPassword } = req.body;
      const tokenRow = await passwordResetRepository.findActiveByRawToken(token);
      if (!tokenRow) {
        return res.status(400).json({
          success: false,
          message: 'Invalid or expired reset token',
        });
      }
      if (tokenRow.used_at || Date.now() > new Date(tokenRow.expires_at).getTime()) {
        return res.status(400).json({
          success: false,
          message: 'Invalid or expired reset token',
        });
      }

      const hash = await bcrypt.hash(newPassword, 12);
      await userRepository.updatePassword(tokenRow.user_id, hash);
      await passwordResetRepository.markUsed(tokenRow.id);

      return res.json({
        success: true,
        message: 'Password has been reset successfully',
      });
    } catch (error) {
      next(error);
    }
  }
);

app.post(
  '/api/auth/oauth/google',
  authLimiter,
  [body('idToken').notEmpty().withMessage('idToken is required')],
  async (req, res, next) => {
    try {
      const invalid = handleValidationErrors(req, res);
      if (invalid) return;
      const profile = await verifyGoogleIdToken(req.body.idToken);
      const user = await findOrCreateOAuthUser(
        {
          sub: profile.sub,
          email: profile.email,
          name: profile.name,
          email_verified: profile.email_verified,
        },
        'google'
      );
      const token = generateToken(user);
      res.json({
        success: true,
        message: 'Signed in with Google',
        token,
        user: sanitizeUser(user),
      });
    } catch (error) {
      next(error);
    }
  }
);

app.post(
  '/api/auth/oauth/facebook',
  authLimiter,
  [body('accessToken').notEmpty().withMessage('accessToken is required')],
  async (req, res, next) => {
    try {
      const invalid = handleValidationErrors(req, res);
      if (invalid) return;
      const profile = await verifyFacebookAccessToken(req.body.accessToken);
      const user = await findOrCreateOAuthUser(
        {
          sub: String(profile.sub),
          email: profile.email,
          name: profile.name,
        },
        'facebook'
      );
      const token = generateToken(user);
      res.json({
        success: true,
        message: 'Signed in with Facebook',
        token,
        user: sanitizeUser(user),
      });
    } catch (error) {
      next(error);
    }
  }
);

app.get('/api/auth/oauth/apple/callback', async (req, res, next) => {
  try {
    const { code, error, error_description: errorDesc } = req.query;
    const appScheme = process.env.OAUTH_ANDROID_APP_SCHEME || 'com.apkprevention.auth';
    const redirectBase = `${appScheme}://oauth`;

    if (error) {
      const msg = encodeURIComponent(
        String(errorDesc || error || 'Apple sign-in was cancelled')
      );
      return res.redirect(302, `${redirectBase}?error=${msg}`);
    }

    if (!code) {
      return res.redirect(302, `${redirectBase}?error=${encodeURIComponent('Missing authorization code')}`);
    }

    const redirectUri =
      process.env.APPLE_REDIRECT_URI ||
      `${process.env.PUBLIC_BASE_URL}/api/auth/oauth/apple/callback`;

    const profile = await exchangeAppleAuthorizationCode(code, redirectUri);
    const pseudoProfile = {
      sub: profile.sub,
      email: profile.email,
      name: profile.email ? profile.email.split('@')[0] : 'Apple User',
      email_verified: profile.email_verified,
    };
    const user = await findOrCreateOAuthUser(pseudoProfile, 'apple');
    const token = generateToken(user);
    res.redirect(302, `${redirectBase}?token=${encodeURIComponent(token)}`);
  } catch (err) {
    next(err);
  }
});

app.post('/api/auth/oauth/apple', authLimiter, async (req, res, next) => {
  try {
    let profile;
    if (req.body.identityToken) {
      profile = await verifyAppleIdentityToken(req.body.identityToken);
    } else if (req.body.authorizationCode && req.body.redirectUri) {
      profile = await exchangeAppleAuthorizationCode(
        req.body.authorizationCode,
        req.body.redirectUri
      );
    } else {
      return res.status(400).json({
        success: false,
        message:
          'Send identityToken (native) or authorizationCode + redirectUri (web/code flow)',
      });
    }
    const pseudoProfile = {
      sub: profile.sub,
      email: profile.email,
      name: profile.email ? profile.email.split('@')[0] : 'Apple User',
      email_verified: profile.email_verified,
    };
    const user = await findOrCreateOAuthUser(pseudoProfile, 'apple');
    const token = generateToken(user);
    res.json({
      success: true,
      message: 'Signed in with Apple',
      token,
      user: sanitizeUser(user),
    });
  } catch (error) {
    next(error);
  }
});

app.get('/api/health', async (req, res) => {
  try {
    await pool.query('SELECT 1 AS ok');
    res.json({
      status: 'ok',
      database: 'connected',
      uptime: process.uptime(),
    });
  } catch (err) {
    res.status(503).json({
      status: 'degraded',
      database: 'disconnected',
      message: NODE_ENV === 'production' ? 'Service unavailable' : err.message,
    });
  }
});

// --- Global Error Handler ---
app.use((err, req, res, next) => {
  console.error('[SERVER ERROR]', err.message);
  const status = err.status || err.statusCode || 500;
  res.status(status).json({
    success: false,
    message: NODE_ENV === 'production' && status >= 500 ? 'An unexpected error occurred' : err.message,
  });
});

async function start() {
  try {
    await pool.query('SELECT 1');
    await runMigrations();
    console.log('[db] PostgreSQL connected; schema is up to date');

    exitIfProductionInvalid();
  } catch (err) {
    console.error('[db] Failed to connect or migrate:', err.message);
    console.error('Check PGHOST, PGPORT, PGUSER, PGPASSWORD, PGDATABASE in .env and that PostgreSQL is running.');
    process.exit(1);
  }

  const server = app.listen(PORT, '0.0.0.0', () => {
    console.log(`\n  APK Prevention Auth Server (${NODE_ENV})`);
    console.log(`  Listening on http://0.0.0.0:${PORT} (emulator: http://10.0.2.2:${PORT})\n`);
  });

  async function shutdown() {
    console.log('Shutting down...');
    try {
      await pool.end();
    } catch (e) {
      console.error(e);
    }
    server.close(() => process.exit(0));
  }

  process.on('SIGTERM', shutdown);
  process.on('SIGINT', shutdown);
}

start();
