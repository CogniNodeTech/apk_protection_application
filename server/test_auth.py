"""Tests for /auth routes (in-memory store reset per test)."""
from __future__ import annotations

import pytest
from fastapi.testclient import TestClient

import auth_routes
from main import app


@pytest.fixture(autouse=True)
def _reset_auth_store() -> None:
    # Keep tests isolated in-memory only; never mutate the persistent on-disk user DB.
    auth_routes._users.clear()
    auth_routes._otp.clear()
    yield
    auth_routes._users.clear()
    auth_routes._otp.clear()


@pytest.fixture
def client() -> TestClient:
    return TestClient(app)


def test_register_login_round_trip(client: TestClient) -> None:
    r = client.post(
        "/auth/register",
        json={
            "fullName": "Test User",
            "email": "t@example.com",
            "phone": "+15550001",
            "password": "password12",
        },
    )
    assert r.status_code == 200
    body = r.json()
    assert body["success"] is True
    assert body["token"]
    assert body["user"]["email"] == "t@example.com"

    r2 = client.post(
        "/auth/login",
        json={"email": "t@example.com", "password": "password12"},
    )
    assert r2.status_code == 200
    assert r2.json()["token"]


def test_register_duplicate_email(client: TestClient) -> None:
    payload = {
        "fullName": "A",
        "email": "dup@example.com",
        "phone": "+15550002",
        "password": "password12",
    }
    assert client.post("/auth/register", json=payload).status_code == 200
    r = client.post("/auth/register", json=payload)
    assert r.status_code == 409
    assert r.json()["success"] is False


def test_send_and_verify_otp(client: TestClient) -> None:
    client.post(
        "/auth/register",
        json={
            "fullName": "Otp User",
            "email": "o@example.com",
            "phone": "+15550003",
            "password": "password12",
        },
    )
    send = client.post("/auth/send-otp", json={"phone": "+15550003"})
    assert send.status_code == 200
    # Dev server logs OTP; read from store for test
    code = auth_routes._otp["+15550003"][0]
    bad = client.post(
        "/auth/verify-otp",
        json={"phone": "+15550003", "code": "000000"},
    )
    assert bad.status_code == 400

    ok = client.post(
        "/auth/verify-otp",
        json={"phone": "+15550003", "code": code},
    )
    assert ok.status_code == 200
    data = ok.json()
    assert data["success"] is True
    assert data["token"]
    assert data["user"]["email"] == "o@example.com"


def test_users_are_persisted_to_disk(client: TestClient) -> None:
    r = client.post(
        "/auth/register",
        json={
            "fullName": "Persist User",
            "email": "persist@example.com",
            "phone": "+15550005",
            "password": "password12",
        },
    )
    assert r.status_code == 200

    # Simulate process restart by clearing in-memory store then reloading from disk.
    auth_routes._users.clear()
    auth_routes._load_users()
    assert "persist@example.com" in auth_routes._users

    login = client.post(
        "/auth/login",
        json={"email": "persist@example.com", "password": "password12"},
    )
    assert login.status_code == 200
    assert login.json()["success"] is True


def test_reset_password_is_generic_for_unknown_email(client: TestClient) -> None:
    r = client.post("/auth/reset-password", json={"email": "missing@example.com"})
    assert r.status_code == 200
    body = r.json()
    assert body["success"] is True
    assert "If an account exists" in body["message"]
    assert body.get("debugResetToken") is None


def test_reset_password_confirm_changes_login_password(client: TestClient) -> None:
    email = "resetme@example.com"
    client.post(
        "/auth/register",
        json={
            "fullName": "Reset Me",
            "email": email,
            "phone": "+15550008",
            "password": "password12",
        },
    )
    reset = client.post("/auth/reset-password", json={"email": email})
    assert reset.status_code == 200
    token = reset.json().get("debugResetToken")
    assert token

    confirm = client.post(
        "/auth/reset-password/confirm",
        json={"token": token, "newPassword": "newpassword34"},
    )
    assert confirm.status_code == 200
    assert confirm.json()["success"] is True

    old_login = client.post("/auth/login", json={"email": email, "password": "password12"})
    assert old_login.status_code == 401
    new_login = client.post("/auth/login", json={"email": email, "password": "newpassword34"})
    assert new_login.status_code == 200
    assert new_login.json()["success"] is True


def test_reset_password_uses_smtp_and_hides_debug_token_when_sent(client: TestClient, monkeypatch: pytest.MonkeyPatch) -> None:
    email = "mailcheck@example.com"
    client.post(
        "/auth/register",
        json={
            "fullName": "Mail Check",
            "email": email,
            "phone": "+15550010",
            "password": "password12",
        },
    )

    calls: list[tuple[str, str]] = []

    def _fake_send(to_email: str, token: str) -> bool:
        calls.append((to_email, token))
        return True

    monkeypatch.setattr(auth_routes, "_send_password_reset_email", _fake_send)
    monkeypatch.setattr(auth_routes, "AUTH_DEBUG_RETURN_RESET_TOKEN", False)

    r = client.post("/auth/reset-password", json={"email": email})
    assert r.status_code == 200
    body = r.json()
    assert body["success"] is True
    assert body.get("debugResetToken") is None
    assert len(calls) == 1
    assert calls[0][0] == email
