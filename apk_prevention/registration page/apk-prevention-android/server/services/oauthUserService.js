const userRepository = require('../repositories/userRepository');

/**
 * @param {{ sub: string, email?: string, name?: string, email_verified?: boolean }} profile
 * @param {'google'|'facebook'|'apple'} provider
 */
async function findOrCreateOAuthUser(profile, provider) {
  const existing = await userRepository.findByOAuth(provider, profile.sub);
  if (existing) return existing;

  let email = profile.email?.trim();
  if (!email) {
    if (provider === 'apple') {
      email = `${profile.sub}@appleoauth.local`;
    } else {
      const err = new Error(
        'Email permission is required from the sign-in provider. Grant email access and try again.'
      );
      err.status = 400;
      throw err;
    }
  }

  const byEmail = await userRepository.findByEmail(email);
  if (byEmail) {
    if (byEmail.password_hash && !byEmail.oauth_provider) {
      const err = new Error(
        'This email is already registered with a password. Sign in with email and password.'
      );
      err.status = 409;
      throw err;
    }
    if (byEmail.oauth_provider && byEmail.oauth_provider !== provider) {
      const err = new Error('This email is already linked to another sign-in method.');
      err.status = 409;
      throw err;
    }
    if (byEmail.oauth_provider === provider && byEmail.oauth_sub === profile.sub) {
      return byEmail;
    }
    const err = new Error('Account already exists.');
    err.status = 409;
    throw err;
  }

  const fullName = profile.name || email.split('@')[0] || 'User';

  return userRepository.createOAuthUser({
    fullName,
    email,
    provider,
    sub: profile.sub,
    phone: null,
  });
}

module.exports = { findOrCreateOAuthUser };
