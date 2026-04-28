"""
User authentication API (register, login, OTP) for SafeGuard Android.

In-memory store for development; replace with a database and real SMS/email in production.
"""
from __future__ import annotations

import hashlib
import json
import logging
import os
import re
import secrets
import smtplib
import threading
import time
from datetime import datetime, timedelta, timezone
from email.message import EmailMessage
from typing import Any

import jwt
from fastapi import APIRouter
from fastapi.responses import JSONResponse
from pydantic import BaseModel, ConfigDict, Field

logger = logging.getLogger(__name__)

AUTH_JWT_SECRET = os.environ.get(
    "AUTH_JWT_SECRET",
    "dev-insecure-change-me-minimum-32-chars-for-hs256!!",
).strip()
AUTH_TOKEN_TTL_DAYS = int(os.environ.get("AUTH_TOKEN_TTL_DAYS", "7"))
AUTH_DEBUG_PRINT_OTP = os.environ.get("AUTH_DEBUG_PRINT_OTP", "1").strip().lower() in {"1", "true", "yes", "y"}
AUTH_DEBUG_RETURN_RESET_TOKEN = os.environ.get("AUTH_DEBUG_RETURN_RESET_TOKEN", "0").strip().lower() in {"1", "true", "yes", "y"}
AUTH_PASSWORD_RESET_TOKEN_TTL_SECONDS = int(os.environ.get("AUTH_PASSWORD_RESET_TOKEN_TTL_SECONDS", "900"))
AUTH_SMTP_HOST = os.environ.get("AUTH_SMTP_HOST", "").strip()
AUTH_SMTP_PORT = int(os.environ.get("AUTH_SMTP_PORT", "587"))
AUTH_SMTP_USERNAME = os.environ.get("AUTH_SMTP_USERNAME", "").strip()
AUTH_SMTP_PASSWORD = os.environ.get("AUTH_SMTP_PASSWORD", "").strip()
AUTH_SMTP_FROM_EMAIL = os.environ.get("AUTH_SMTP_FROM_EMAIL", AUTH_SMTP_USERNAME).strip()
AUTH_SMTP_FROM_NAME = os.environ.get("AUTH_SMTP_FROM_NAME", "AEGISNODE Security").strip()
AUTH_SMTP_USE_TLS = os.environ.get("AUTH_SMTP_USE_TLS", "1").strip().lower() in {"1", "true", "yes", "y"}
AUTH_SMTP_USE_SSL = os.environ.get("AUTH_SMTP_USE_SSL", "0").strip().lower() in {"1", "true", "yes", "y"}
AUTH_RESET_LINK_BASE_URL = os.environ.get("AUTH_RESET_LINK_BASE_URL", "").strip()

router = APIRouter(prefix="/auth", tags=["auth"])

_lock = threading.Lock()
_users: dict[str, dict[str, Any]] = {}  # email (lower) -> user record
_otp: dict[str, tuple[str, float]] = {}  # phone -> (code, expiry_epoch)
_password_reset_tokens: dict[str, tuple[str, float]] = {}  # token -> (email, expiry_epoch)
AUTH_USER_DB_PATH = os.environ.get(
    "AUTH_USER_DB_PATH",
    os.path.join(os.path.dirname(__file__), "auth_users.json"),
).strip()


class RegisterRequest(BaseModel):
    model_config = ConfigDict(extra="ignore")

    fullName: str = Field(..., min_length=1)
    email: str
    phone: str = Field(..., min_length=5)
    password: str = Field(..., min_length=8)


class LoginRequest(BaseModel):
    model_config = ConfigDict(extra="ignore")

    email: str
    password: str


class OtpSendRequest(BaseModel):
    model_config = ConfigDict(extra="ignore")

    phone: str = Field(..., min_length=5)


class OtpVerifyRequest(BaseModel):
    model_config = ConfigDict(extra="ignore")

    phone: str = Field(..., min_length=5)
    code: str = Field(..., min_length=4, max_length=10)


class ResetPasswordRequest(BaseModel):
    model_config = ConfigDict(extra="ignore")

    email: str


class ResetPasswordConfirmRequest(BaseModel):
    model_config = ConfigDict(extra="ignore")

    token: str = Field(..., min_length=8)
    newPassword: str = Field(..., min_length=8)


class OAuthGoogleRequest(BaseModel):
    model_config = ConfigDict(extra="ignore")

    idToken: str = Field(..., min_length=8)


class UserDto(BaseModel):
    id: str
    fullName: str
    email: str
    phone: str


class AuthResponse(BaseModel):
    success: bool
    message: str
    token: str | None = None
    user: UserDto | None = None


class OtpResponse(BaseModel):
    success: bool
    message: str
    debugResetToken: str | None = None


def _norm_email(email: str) -> str:
    return email.strip().lower()


def _hash_pw(password: str, salt: bytes) -> str:
    return hashlib.pbkdf2_hmac("sha256", password.encode("utf-8"), salt, 100_000).hex()


def _smtp_configured() -> bool:
    return bool(AUTH_SMTP_HOST and AUTH_SMTP_FROM_EMAIL)


def _password_reset_link(token: str) -> str:
    base = AUTH_RESET_LINK_BASE_URL.strip()
    if not base:
        return token
    sep = "&" if "?" in base else "?"
    return f"{base}{sep}token={token}"


def _send_password_reset_email(to_email: str, token: str) -> bool:
    if not _smtp_configured():
        return False
    reset_link = _password_reset_link(token)
    msg = EmailMessage()
    msg["Subject"] = "AEGISNODE Password Reset"
    msg["From"] = f"{AUTH_SMTP_FROM_NAME} <{AUTH_SMTP_FROM_EMAIL}>"
    msg["To"] = to_email
    msg.set_content(
        "We received a password reset request for your AEGISNODE account.\n\n"
        f"Use this reset link/token within {AUTH_PASSWORD_RESET_TOKEN_TTL_SECONDS // 60} minutes:\n"
        f"{reset_link}\n\n"
        "If you did not request this, you can ignore this email."
    )
    try:
        if AUTH_SMTP_USE_SSL:
            with smtplib.SMTP_SSL(AUTH_SMTP_HOST, AUTH_SMTP_PORT, timeout=15) as smtp:
                if AUTH_SMTP_USERNAME and AUTH_SMTP_PASSWORD:
                    smtp.login(AUTH_SMTP_USERNAME, AUTH_SMTP_PASSWORD)
                smtp.send_message(msg)
        else:
            with smtplib.SMTP(AUTH_SMTP_HOST, AUTH_SMTP_PORT, timeout=15) as smtp:
                if AUTH_SMTP_USE_TLS:
                    smtp.starttls()
                if AUTH_SMTP_USERNAME and AUTH_SMTP_PASSWORD:
                    smtp.login(AUTH_SMTP_USERNAME, AUTH_SMTP_PASSWORD)
                smtp.send_message(msg)
        return True
    except Exception as e:
        logger.warning("auth: failed to send reset email to %s: %s", to_email, e)
        return False


def _persist_users() -> None:
    """Durably write users so auth survives server restarts/reinstalls."""
    payload = {
        email: {
            "id": rec["id"],
            "fullName": rec["fullName"],
            "email": rec["email"],
            "phone": rec["phone"],
            "salt_hex": rec["salt_hex"],
            "password_hash": rec["password_hash"],
        }
        for email, rec in _users.items()
    }
    tmp = f"{AUTH_USER_DB_PATH}.tmp"
    os.makedirs(os.path.dirname(AUTH_USER_DB_PATH), exist_ok=True)
    with open(tmp, "w", encoding="utf-8") as f:
        json.dump(payload, f, ensure_ascii=True)
    os.replace(tmp, AUTH_USER_DB_PATH)


def _load_users() -> None:
    if not os.path.exists(AUTH_USER_DB_PATH):
        return
    try:
        with open(AUTH_USER_DB_PATH, "r", encoding="utf-8") as f:
            raw = json.load(f)
        if not isinstance(raw, dict):
            logger.warning("auth: ignoring invalid user db payload")
            return
        loaded: dict[str, dict[str, Any]] = {}
        for email, rec in raw.items():
            if not isinstance(rec, dict):
                continue
            salt_hex = str(rec.get("salt_hex", "")).strip().lower()
            password_hash = str(rec.get("password_hash", "")).strip().lower()
            if not salt_hex or not password_hash:
                continue
            loaded[_norm_email(email)] = {
                "id": str(rec.get("id", "")).strip() or secrets.token_hex(8),
                "fullName": str(rec.get("fullName", "")).strip() or "User",
                "email": _norm_email(str(rec.get("email", email))),
                "phone": str(rec.get("phone", "")).strip(),
                "salt_hex": salt_hex,
                "password_hash": password_hash,
            }
        _users.clear()
        _users.update(loaded)
        logger.info("auth: loaded %d persisted users", len(_users))
    except Exception as e:
        logger.warning("auth: failed to load persisted users: %s", e)


def _issue_token(email: str, user_id: str) -> str:
    exp = datetime.now(timezone.utc) + timedelta(days=AUTH_TOKEN_TTL_DAYS)
    return jwt.encode(
        {"sub": email, "uid": user_id, "exp": exp},
        AUTH_JWT_SECRET,
        algorithm="HS256",
    )


def _json_err(status: int, message: str) -> JSONResponse:
    return JSONResponse(status_code=status, content={"success": False, "message": message})


_load_users()


@router.post("/register", response_model=AuthResponse)
def register(body: RegisterRequest) -> AuthResponse | JSONResponse:
    email = _norm_email(body.email)
    if not re.match(r"^[^@]+@[^@]+\.[^@]+$", email):
        return _json_err(400, "Invalid email address")

    salt = secrets.token_bytes(16)
    uid = secrets.token_hex(8)
    rec = {
        "id": uid,
        "fullName": body.fullName.strip(),
        "email": email,
        "phone": body.phone.strip(),
        "salt_hex": salt.hex(),
        "password_hash": _hash_pw(body.password, salt),
    }

    with _lock:
        if email in _users:
            return _json_err(409, "This email is already registered.")
        _users[email] = rec
        _persist_users()

    token = _issue_token(email, uid)
    user = UserDto(
        id=uid,
        fullName=rec["fullName"],
        email=email,
        phone=rec["phone"],
    )
    logger.info("auth: registered user %s", email)
    return AuthResponse(success=True, message="Account created.", token=token, user=user)


@router.post("/login", response_model=AuthResponse)
def login(body: LoginRequest) -> AuthResponse | JSONResponse:
    email = _norm_email(body.email)
    with _lock:
        rec = _users.get(email)
    if not rec:
        return _json_err(401, "Invalid email or password.")
    if not secrets.compare_digest(rec["password_hash"], _hash_pw(body.password, bytes.fromhex(rec["salt_hex"]))):
        return _json_err(401, "Invalid email or password.")

    token = _issue_token(email, rec["id"])
    user = UserDto(
        id=rec["id"],
        fullName=rec["fullName"],
        email=email,
        phone=rec["phone"],
    )
    return AuthResponse(success=True, message="Signed in.", token=token, user=user)


@router.post("/send-otp", response_model=OtpResponse)
def send_otp(body: OtpSendRequest) -> OtpResponse:
    phone = body.phone.strip()
    code = f"{secrets.randbelow(1_000_000):06d}"
    exp = time.time() + 600.0
    with _lock:
        _otp[phone] = (code, exp)
    if AUTH_DEBUG_PRINT_OTP:
        logger.warning("auth: OTP for %s is %s (dev only)", phone, code)
    return OtpResponse(success=True, message="Verification code sent.")


@router.post("/verify-otp", response_model=AuthResponse)
def verify_otp(body: OtpVerifyRequest) -> AuthResponse | JSONResponse:
    """Validate OTP. Issues a session token for the user registered with this phone (dev convenience)."""
    phone = body.phone.strip()
    code = body.code.strip()
    now = time.time()
    with _lock:
        entry = _otp.get(phone)
        if not entry:
            return _json_err(400, "No verification pending for this number.")
        stored, exp = entry
        if now > exp:
            del _otp[phone]
            return _json_err(400, "Code expired. Request a new one.")
        if not secrets.compare_digest(stored, code):
            return _json_err(400, "Invalid verification code.")
        del _otp[phone]
        # Find user by phone (first match)
        rec = next((u for u in _users.values() if u["phone"] == phone), None)

    if not rec:
        return AuthResponse(
            success=True,
            message="Phone verified.",
            token=None,
            user=None,
        )
    # Registered user: return session token
    email = rec["email"]
    token = _issue_token(email, rec["id"])
    user = UserDto(
        id=rec["id"],
        fullName=rec["fullName"],
        email=email,
        phone=rec["phone"],
    )
    return AuthResponse(success=True, message="Phone verified.", token=token, user=user)


@router.post("/reset-password", response_model=OtpResponse)
def reset_password(body: ResetPasswordRequest) -> OtpResponse:
    """
    Issue a one-time reset token and acknowledge generically.

    In production this token must be delivered via email/SMS and never returned to client.
    In local dev, [AUTH_DEBUG_RETURN_RESET_TOKEN] lets the app complete reset end-to-end.
    """
    email = _norm_email(body.email)
    token_to_send: str | None = None
    debug_token: str | None = None
    with _lock:
        rec = _users.get(email)
        if rec is not None:
            token = secrets.token_urlsafe(32)
            exp = time.time() + float(AUTH_PASSWORD_RESET_TOKEN_TTL_SECONDS)
            _password_reset_tokens[token] = (email, exp)
            token_to_send = token
    if token_to_send is not None:
        email_sent = _send_password_reset_email(email, token_to_send)
        if AUTH_DEBUG_RETURN_RESET_TOKEN or not email_sent:
            debug_token = token_to_send
    return OtpResponse(
        success=True,
        message="If an account exists, reset instructions would be sent.",
        debugResetToken=debug_token,
    )


@router.post("/reset-password/confirm", response_model=OtpResponse)
def confirm_reset_password(body: ResetPasswordConfirmRequest) -> OtpResponse | JSONResponse:
    token = body.token.strip()
    now = time.time()
    with _lock:
        entry = _password_reset_tokens.get(token)
        if not entry:
            return _json_err(400, "Invalid or expired reset token.")
        email, exp = entry
        if now > exp:
            _password_reset_tokens.pop(token, None)
            return _json_err(400, "Invalid or expired reset token.")
        rec = _users.get(email)
        if rec is None:
            _password_reset_tokens.pop(token, None)
            return _json_err(400, "Invalid or expired reset token.")
        salt = secrets.token_bytes(16)
        rec["salt_hex"] = salt.hex()
        rec["password_hash"] = _hash_pw(body.newPassword, salt)
        _users[email] = rec
        _password_reset_tokens.pop(token, None)
        _persist_users()
    return OtpResponse(success=True, message="Password reset successful.")


@router.post("/oauth/google", response_model=AuthResponse)
def oauth_google(body: OAuthGoogleRequest) -> AuthResponse | JSONResponse:
    """
    Development Google OAuth exchange endpoint.

    The Android app obtains an ID token via Google Sign-In and posts it here. In
    production this endpoint must verify the token signature/audience against Google's
    JWKS and extract the subject/email claims. For local development we accept any
    non-empty token and mint (or re-use) a deterministic local user profile so the
    sign-in flow is testable end-to-end without external verifier dependencies.
    """
    token = body.idToken.strip()
    if len(token) < 8:
        return _json_err(400, "Invalid Google ID token.")

    # Stable dev identity from token hash (no plaintext token storage).
    h = hashlib.sha256(token.encode("utf-8")).hexdigest()
    email = f"google_{h[:10]}@aegisnode.local"
    uid = f"g_{h[:16]}"

    with _lock:
        rec = _users.get(email)
        if rec is None:
            salt = secrets.token_bytes(16)
            rec = {
                "id": uid,
                "fullName": "Google User",
                "email": email,
                "phone": "",
                "salt_hex": salt.hex(),
                "password_hash": _hash_pw(uid, salt),
            }
            _users[email] = rec
            _persist_users()

    session = _issue_token(email, rec["id"])
    user = UserDto(
        id=rec["id"],
        fullName=rec["fullName"],
        email=email,
        phone=rec["phone"],
    )
    return AuthResponse(success=True, message="Google sign-in successful.", token=session, user=user)
