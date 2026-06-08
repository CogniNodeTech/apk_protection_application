"""
User authentication API (register, login, OTP) for SafeGuard Android.
Utilizes SQLAlchemy to support MySQL or SQLite database backend configurations.
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
from fastapi import APIRouter, Depends
from fastapi.responses import JSONResponse
from pydantic import BaseModel, ConfigDict, Field
from sqlalchemy import Column, Float, String
from sqlalchemy.orm import Session

from database import Base, get_db, get_db_session, init_db

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


# ── Database Model Definitions ────────────────────────────────────────────────

class User(Base):
    __tablename__ = "users"

    id = Column(String(50), primary_key=True)
    fullName = Column(String(100), nullable=False)
    email = Column(String(150), unique=True, nullable=False, index=True)
    phone = Column(String(50), nullable=False)
    salt_hex = Column(String(64), nullable=False)
    password_hash = Column(String(128), nullable=False)


class Otp(Base):
    __tablename__ = "otps"

    phone = Column(String(50), primary_key=True)
    code = Column(String(20), nullable=False)
    expires_at = Column(Float, nullable=False)


class PasswordReset(Base):
    __tablename__ = "password_resets"

    token = Column(String(100), primary_key=True)
    email = Column(String(150), nullable=False)
    expires_at = Column(Float, nullable=False)


# Initialize Database Tables
init_db()


# ── Dict-like Compatibility Layers for PyTest Fixtures and Legacy logic ────────

class UsersDictCompat:
    def clear(self) -> None:
        with get_db_session() as db:
            db.query(User).delete()

    def __contains__(self, email: str) -> bool:
        with get_db_session() as db:
            return db.query(User).filter(User.email == email.strip().lower()).first() is not None

    def __setitem__(self, email: str, value: dict[str, Any]) -> None:
        with get_db_session() as db:
            email_norm = email.strip().lower()
            existing = db.query(User).filter(User.email == email_norm).first()
            if existing:
                existing.fullName = value["fullName"]
                existing.phone = value["phone"]
                existing.salt_hex = value["salt_hex"]
                existing.password_hash = value["password_hash"]
            else:
                db.add(
                    User(
                        id=value["id"],
                        fullName=value["fullName"],
                        email=email_norm,
                        phone=value["phone"],
                        salt_hex=value["salt_hex"],
                        password_hash=value["password_hash"],
                    )
                )
            db.commit()

    def get(self, email: str) -> dict[str, Any] | None:
        with get_db_session() as db:
            user = db.query(User).filter(User.email == email.strip().lower()).first()
            if not user:
                return None
            return {
                "id": user.id,
                "fullName": user.fullName,
                "email": user.email,
                "phone": user.phone,
                "salt_hex": user.salt_hex,
                "password_hash": user.password_hash,
            }

    def values(self) -> list[dict[str, Any]]:
        with get_db_session() as db:
            users = db.query(User).all()
            return [
                {
                    "id": u.id,
                    "fullName": u.fullName,
                    "email": u.email,
                    "phone": u.phone,
                    "salt_hex": u.salt_hex,
                    "password_hash": u.password_hash,
                }
                for u in users
            ]


class OtpDictCompat:
    def clear(self) -> None:
        with get_db_session() as db:
            db.query(Otp).delete()

    def __setitem__(self, phone: str, value: tuple[str, float]) -> None:
        with get_db_session() as db:
            phone_norm = phone.strip()
            db.query(Otp).filter(Otp.phone == phone_norm).delete()
            db.add(Otp(phone=phone_norm, code=value[0], expires_at=value[1]))
            db.commit()

    def __getitem__(self, phone: str) -> tuple[str, float]:
        with get_db_session() as db:
            otp = db.query(Otp).filter(Otp.phone == phone.strip()).first()
            if not otp:
                raise KeyError(phone)
            return (otp.code, otp.expires_at)

    def get(self, phone: str) -> tuple[str, float] | None:
        with get_db_session() as db:
            otp = db.query(Otp).filter(Otp.phone == phone.strip()).first()
            if not otp:
                return None
            return (otp.code, otp.expires_at)

    def __delitem__(self, phone: str) -> None:
        with get_db_session() as db:
            db.query(Otp).filter(Otp.phone == phone.strip()).delete()
            db.commit()


class PasswordResetDictCompat:
    def clear(self) -> None:
        with get_db_session() as db:
            db.query(PasswordReset).delete()

    def __setitem__(self, token: str, val: tuple[str, float]) -> None:
        with get_db_session() as db:
            db.query(PasswordReset).filter(PasswordReset.token == token).delete()
            db.add(PasswordReset(token=token, email=val[0], expires_at=val[1]))
            db.commit()

    def __getitem__(self, token: str) -> tuple[str, float]:
        with get_db_session() as db:
            entry = db.query(PasswordReset).filter(PasswordReset.token == token).first()
            if not entry:
                raise KeyError(token)
            return (entry.email, entry.expires_at)

    def get(self, token: str) -> tuple[str, float] | None:
        with get_db_session() as db:
            entry = db.query(PasswordReset).filter(PasswordReset.token == token).first()
            if not entry:
                return None
            return (entry.email, entry.expires_at)

    def pop(self, token: str, default: Any = None) -> tuple[str, float] | None:
        with get_db_session() as db:
            entry = db.query(PasswordReset).filter(PasswordReset.token == token).first()
            if entry:
                val = (entry.email, entry.expires_at)
                db.delete(entry)
                db.commit()
                return val
            return default


_lock = threading.Lock()
_users = UsersDictCompat()
_otp = OtpDictCompat()
_password_reset_tokens = PasswordResetDictCompat()

AUTH_USER_DB_PATH = os.environ.get(
    "AUTH_USER_DB_PATH",
    os.path.join(os.path.dirname(__file__), "auth_users.json"),
).strip()


# ── Pydantic Request/Response DTOs ────────────────────────────────────────────

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


# ── Helper Functions ──────────────────────────────────────────────────────────

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
    """Durably write users to backup JSON so legacy file-monitoring tests pass."""
    with get_db_session() as db:
        users = db.query(User).all()
        payload = {
            u.email: {
                "id": u.id,
                "fullName": u.fullName,
                "email": u.email,
                "phone": u.phone,
                "salt_hex": u.salt_hex,
                "password_hash": u.password_hash,
            }
            for u in users
        }
    tmp = f"{AUTH_USER_DB_PATH}.tmp"
    os.makedirs(os.path.dirname(AUTH_USER_DB_PATH), exist_ok=True)
    with open(tmp, "w", encoding="utf-8") as f:
        json.dump(payload, f, ensure_ascii=True)
    os.replace(tmp, AUTH_USER_DB_PATH)


def _load_users() -> None:
    """Load users from backup JSON into database (process start simulation)."""
    if not os.path.exists(AUTH_USER_DB_PATH):
        return
    try:
        with open(AUTH_USER_DB_PATH, "r", encoding="utf-8") as f:
            raw = json.load(f)
        if not isinstance(raw, dict):
            logger.warning("auth: ignoring invalid user db payload")
            return
        with get_db_session() as db:
            for email, rec in raw.items():
                if not isinstance(rec, dict):
                    continue
                salt_hex = str(rec.get("salt_hex", "")).strip().lower()
                password_hash = str(rec.get("password_hash", "")).strip().lower()
                if not salt_hex or not password_hash:
                    continue
                email_norm = _norm_email(str(rec.get("email", email)))
                existing = db.query(User).filter(User.email == email_norm).first()
                if not existing:
                    db.add(
                        User(
                            id=str(rec.get("id", "")).strip() or secrets.token_hex(8),
                            fullName=str(rec.get("fullName", "")).strip() or "User",
                            email=email_norm,
                            phone=str(rec.get("phone", "")).strip(),
                            salt_hex=salt_hex,
                            password_hash=password_hash,
                        )
                    )
            db.commit()
        logger.info("auth: loaded users from backup JSON")
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


# Run initial boot load
_load_users()


# ── Route Handlers ────────────────────────────────────────────────────────────

@router.post("/register", response_model=AuthResponse)
def register(body: RegisterRequest, db: Session = Depends(get_db)) -> AuthResponse | JSONResponse:
    email = _norm_email(body.email)
    if not re.match(r"^[^@]+@[^@]+\.[^@]+$", email):
        return _json_err(400, "Invalid email address")

    # DB unique check
    existing = db.query(User).filter(User.email == email).first()
    if existing:
        return _json_err(409, "This email is already registered.")

    salt = secrets.token_bytes(16)
    uid = secrets.token_hex(8)
    user = User(
        id=uid,
        fullName=body.fullName.strip(),
        email=email,
        phone=body.phone.strip(),
        salt_hex=salt.hex(),
        password_hash=_hash_pw(body.password, salt),
    )
    db.add(user)
    db.commit()

    _persist_users()

    token = _issue_token(email, uid)
    user_dto = UserDto(
        id=uid,
        fullName=user.fullName,
        email=email,
        phone=user.phone,
    )
    logger.info("auth: registered user %s", email)
    return AuthResponse(success=True, message="Account created.", token=token, user=user_dto)


@router.post("/login", response_model=AuthResponse)
def login(body: LoginRequest, db: Session = Depends(get_db)) -> AuthResponse | JSONResponse:
    email = _norm_email(body.email)
    user = db.query(User).filter(User.email == email).first()
    if not user:
        return _json_err(401, "Invalid email or password.")
    if not secrets.compare_digest(user.password_hash, _hash_pw(body.password, bytes.fromhex(user.salt_hex))):
        return _json_err(401, "Invalid email or password.")

    token = _issue_token(email, user.id)
    user_dto = UserDto(
        id=user.id,
        fullName=user.fullName,
        email=email,
        phone=user.phone,
    )
    return AuthResponse(success=True, message="Signed in.", token=token, user=user_dto)


@router.post("/send-otp", response_model=OtpResponse)
def send_otp(body: OtpSendRequest, db: Session = Depends(get_db)) -> OtpResponse:
    phone = body.phone.strip()
    code = f"{secrets.randbelow(1_000_000):06d}"
    exp = time.time() + 600.0

    # UPSERT pattern via SQLAlchemy
    db.query(Otp).filter(Otp.phone == phone).delete()
    db.add(Otp(phone=phone, code=code, expires_at=exp))
    db.commit()

    if AUTH_DEBUG_PRINT_OTP:
        logger.warning("auth: OTP for %s is %s (dev only)", phone, code)
    return OtpResponse(success=True, message="Verification code sent.")


@router.post("/verify-otp", response_model=AuthResponse)
def verify_otp(body: OtpVerifyRequest, db: Session = Depends(get_db)) -> AuthResponse | JSONResponse:
    """Validate OTP. Issues a session token for the user registered with this phone."""
    phone = body.phone.strip()
    code = body.code.strip()
    now = time.time()

    otp_entry = db.query(Otp).filter(Otp.phone == phone).first()
    if not otp_entry:
        return _json_err(400, "No verification pending for this number.")
    if now > otp_entry.expires_at:
        db.delete(otp_entry)
        db.commit()
        return _json_err(400, "Code expired. Request a new one.")
    if not secrets.compare_digest(otp_entry.code, code):
        return _json_err(400, "Invalid verification code.")

    db.delete(otp_entry)
    db.commit()

    # Find user by phone (first match)
    user = db.query(User).filter(User.phone == phone).first()
    if not user:
        return AuthResponse(
            success=True,
            message="Phone verified.",
            token=None,
            user=None,
        )

    # Registered user: return session token
    token = _issue_token(user.email, user.id)
    user_dto = UserDto(
        id=user.id,
        fullName=user.fullName,
        email=user.email,
        phone=user.phone,
    )
    return AuthResponse(success=True, message="Phone verified.", token=token, user=user_dto)


@router.post("/reset-password", response_model=OtpResponse)
def reset_password(body: ResetPasswordRequest, db: Session = Depends(get_db)) -> OtpResponse:
    """
    Issue a one-time reset token and acknowledge generically.
    In local dev, [AUTH_DEBUG_RETURN_RESET_TOKEN] lets the app complete reset end-to-end.
    """
    email = _norm_email(body.email)
    token_to_send: str | None = None
    debug_token: str | None = None

    user = db.query(User).filter(User.email == email).first()
    if user is not None:
        token = secrets.token_urlsafe(32)
        exp = time.time() + float(AUTH_PASSWORD_RESET_TOKEN_TTL_SECONDS)

        # UPSERT password reset token
        db.query(PasswordReset).filter(PasswordReset.email == email).delete()
        db.add(PasswordReset(token=token, email=email, expires_at=exp))
        db.commit()

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
def confirm_reset_password(
    body: ResetPasswordConfirmRequest, db: Session = Depends(get_db)
) -> OtpResponse | JSONResponse:
    token = body.token.strip()
    now = time.time()

    reset_entry = db.query(PasswordReset).filter(PasswordReset.token == token).first()
    if not reset_entry:
        return _json_err(400, "Invalid or expired reset token.")
    if now > reset_entry.expires_at:
        db.delete(reset_entry)
        db.commit()
        return _json_err(400, "Invalid or expired reset token.")

    user = db.query(User).filter(User.email == reset_entry.email).first()
    if user is None:
        db.delete(reset_entry)
        db.commit()
        return _json_err(400, "Invalid or expired reset token.")

    salt = secrets.token_bytes(16)
    user.salt_hex = salt.hex()
    user.password_hash = _hash_pw(body.newPassword, salt)
    db.delete(reset_entry)
    db.commit()

    _persist_users()
    return OtpResponse(success=True, message="Password reset successful.")


@router.post("/oauth/google", response_model=AuthResponse)
def oauth_google(body: OAuthGoogleRequest, db: Session = Depends(get_db)) -> AuthResponse | JSONResponse:
    """
    Development Google OAuth exchange endpoint.
    Mints (or re-uses) a deterministic local user profile from token hash.
    """
    token = body.idToken.strip()
    if len(token) < 8:
        return _json_err(400, "Invalid Google ID token.")

    # Stable dev identity from token hash (no plaintext token storage).
    h = hashlib.sha256(token.encode("utf-8")).hexdigest()
    email = f"google_{h[:10]}@aegisnode.local"
    uid = f"g_{h[:16]}"

    user = db.query(User).filter(User.email == email).first()
    if user is None:
        salt = secrets.token_bytes(16)
        user = User(
            id=uid,
            fullName="Google User",
            email=email,
            phone="",
            salt_hex=salt.hex(),
            password_hash=_hash_pw(uid, salt),
        )
        db.add(user)
        db.commit()
        _persist_users()

    session = _issue_token(email, user.id)
    user_dto = UserDto(
        id=user.id,
        fullName=user.fullName,
        email=email,
        phone=user.phone,
    )
    return AuthResponse(success=True, message="Google sign-in successful.", token=session, user=user_dto)
