const { OAuth2Client } = require('google-auth-library');
const axios = require('axios');
const jwt = require('jsonwebtoken');
const jwksClient = require('jwks-rsa');
const crypto = require('crypto');
const fs = require('fs');

const appleJwks = jwksClient({
  jwksUri: 'https://appleid.apple.com/auth/keys',
  cache: true,
  rateLimit: true,
});

function getAppleSigningKey(header, callback) {
  appleJwks.getSigningKey(header.kid, (err, key) => {
    if (err) return callback(err);
    callback(null, key.getPublicKey());
  });
}

/**
 * @returns {Promise<{ sub: string, email?: string, email_verified?: boolean, name?: string }>}
 */
async function verifyGoogleIdToken(idToken) {
  const clientId = process.env.GOOGLE_CLIENT_ID;
  if (!clientId) {
    throw new Error('GOOGLE_CLIENT_ID is not configured');
  }
  const client = new OAuth2Client(clientId);
  const ticket = await client.verifyIdToken({
    idToken,
    audience: clientId,
  });
  const payload = ticket.getPayload();
  if (!payload || !payload.sub) {
    throw new Error('Invalid Google token');
  }
  return {
    sub: payload.sub,
    email: payload.email,
    email_verified: payload.email_verified,
    name: payload.name,
  };
}

/**
 * @returns {Promise<{ sub: string, email?: string, name?: string }>}
 */
async function verifyFacebookAccessToken(accessToken) {
  const appId = process.env.FACEBOOK_APP_ID;
  const appSecret = process.env.FACEBOOK_APP_SECRET;
  if (!appId || !appSecret) {
    throw new Error('FACEBOOK_APP_ID / FACEBOOK_APP_SECRET not configured');
  }

  const appAccessToken = `${appId}|${appSecret}`;
  const debugUrl = `https://graph.facebook.com/debug_token?input_token=${encodeURIComponent(
    accessToken
  )}&access_token=${encodeURIComponent(appAccessToken)}`;
  const debugRes = await axios.get(debugUrl);
  const data = debugRes.data?.data;
  if (!data?.is_valid || String(data.app_id) !== String(appId)) {
    throw new Error('Invalid Facebook access token');
  }
  const userId = data.user_id;

  const meUrl = `https://graph.facebook.com/me?fields=id,name,email&access_token=${encodeURIComponent(
    accessToken
  )}`;
  const meRes = await axios.get(meUrl);
  const me = meRes.data;
  return {
    sub: userId || me.id,
    email: me.email,
    name: me.name,
  };
}

/**
 * Verify Apple identity token (JWT from native Sign in with Apple).
 * APPLE_CLIENT_ID must match the token audience (Service ID or bundle id).
 */
function verifyAppleIdentityToken(identityToken) {
  const audience = process.env.APPLE_CLIENT_ID;
  if (!audience) {
    throw new Error('APPLE_CLIENT_ID is not configured');
  }
  return new Promise((resolve, reject) => {
    jwt.verify(
      identityToken,
      getAppleSigningKey,
      {
        algorithms: ['RS256'],
        issuer: 'https://appleid.apple.com',
        audience,
      },
      (err, decoded) => {
        if (err) return reject(err);
        if (!decoded || !decoded.sub) return reject(new Error('Invalid Apple token'));
        resolve({
          sub: decoded.sub,
          email: decoded.email,
          email_verified: decoded.email_verified,
        });
      }
    );
  });
}

/**
 * Exchange authorization code for tokens (Android / web redirect flow).
 * Requires APPLE_TEAM_ID, APPLE_KEY_ID, APPLE_CLIENT_ID, APPLE_PRIVATE_KEY (PEM string or path).
 */
async function exchangeAppleAuthorizationCode(authorizationCode, redirectUri) {
  const clientId = process.env.APPLE_CLIENT_ID;
  const teamId = process.env.APPLE_TEAM_ID;
  const keyId = process.env.APPLE_KEY_ID;
  let privateKey = process.env.APPLE_PRIVATE_KEY;
  const keyPath = process.env.APPLE_PRIVATE_KEY_PATH;

  if (!clientId || !teamId || !keyId) {
    throw new Error('Apple OAuth server configuration incomplete');
  }
  if (keyPath && !privateKey) {
    privateKey = fs.readFileSync(keyPath, 'utf8');
  }
  if (!privateKey) {
    throw new Error('APPLE_PRIVATE_KEY or APPLE_PRIVATE_KEY_PATH is required for code exchange');
  }

  const clientSecret = jwt.sign(
    {
      iss: teamId,
      iat: Math.floor(Date.now() / 1000),
      exp: Math.floor(Date.now() / 1000) + 86400 * 180,
      aud: 'https://appleid.apple.com',
      sub: clientId,
    },
    privateKey.replace(/\\n/g, '\n'),
    { algorithm: 'ES256', keyid: keyId }
  );

  const params = new URLSearchParams();
  params.set('client_id', clientId);
  params.set('client_secret', clientSecret);
  params.set('code', authorizationCode);
  params.set('grant_type', 'authorization_code');
  params.set('redirect_uri', redirectUri);

  const tokenRes = await axios.post('https://appleid.apple.com/auth/token', params.toString(), {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  });

  const idToken = tokenRes.data?.id_token;
  if (!idToken) {
    throw new Error('Apple token response missing id_token');
  }

  return verifyAppleIdentityToken(idToken);
}

module.exports = {
  verifyGoogleIdToken,
  verifyFacebookAccessToken,
  verifyAppleIdentityToken,
  exchangeAppleAuthorizationCode,
};
