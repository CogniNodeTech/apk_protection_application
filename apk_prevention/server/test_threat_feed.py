"""
Tests for the bulk MalwareBazaar APK feed (mapping + GET /v1/threat-feed).

These tests never hit the real abuse.ch endpoint. The MalwareBazaar response is monkey-
patched so we can pin behaviour against malformed records, severity mapping, and the
since/limit cursor contract without depending on network state or an auth key.
"""
from __future__ import annotations

import calendar
from datetime import datetime, timezone
from unittest.mock import patch

import pytest
from fastapi.testclient import TestClient

from malwarebazaar import (
    THREAT_FEED_MAX_LIMIT,
    build_threat_feed_response,
    map_recent_to_feed_items,
    severity_from_tags,
)


def _epoch_ms(year: int, month: int, day: int, hour: int = 0, minute: int = 0, second: int = 0) -> int:
    dt = datetime(year, month, day, hour, minute, second, tzinfo=timezone.utc)
    return calendar.timegm(dt.timetuple()) * 1000


def _entry(
    *,
    sha256: str = "a" * 64,
    sha512: str | None = "b" * 128,
    tlsh: str | None = "T1" + "F" * 70,
    signature: str | None = "Anatsa",
    tags: list[str] | str | None = None,
    first_seen: str | None = "2024-06-01 12:34:56",
) -> dict:
    """Compact factory for MalwareBazaar `data` rows used across test cases."""
    base: dict = {
        "sha256_hash": sha256,
        "sha512_hash": sha512,
        "tlsh": tlsh,
        "signature": signature,
        "first_seen": first_seen,
    }
    if tags is not None:
        base["tags"] = tags
    return base


def test_severity_from_tags_picks_highest_match() -> None:
    assert severity_from_tags(["banker", "trojan"]) == 95
    assert severity_from_tags(["trojan", "dropper"]) == 90
    assert severity_from_tags(["adware"]) == 80
    # Unknown tags fall back to the conservative bucket so the row still indexes by TLSH.
    assert severity_from_tags(["random-tag"]) == 70
    assert severity_from_tags([]) == 70


def test_map_recent_drops_rows_without_tlsh_or_sha256() -> None:
    response = {
        "query_status": "ok",
        "data": [
            _entry(sha256="x"),  # invalid sha256 length -> dropped
            _entry(tlsh=None),  # missing tlsh -> dropped (would be unindexable)
            _entry(tlsh="not-tlsh"),  # malformed tlsh -> dropped
            _entry(sha256="c" * 64, tlsh="T1" + "A" * 70),  # valid
        ],
    }
    items = map_recent_to_feed_items(response, since_ms=None, limit=10)
    assert len(items) == 1
    assert items[0]["sha256"] == "c" * 64
    # TLSH is normalised to bare 70-char upper hex (drops the "T1" prefix).
    assert items[0]["fuzzy_hash"] == "A" * 70
    assert items[0]["source"] == "malwarebazaar"


def test_map_recent_severity_uses_tags_then_falls_back() -> None:
    response = {
        "query_status": "ok",
        "data": [
            _entry(sha256="d" * 64, tags=["banker", "android"], signature="Hydra"),
            _entry(sha256="e" * 64, tags=["adware", "ads"], signature=None),
            _entry(sha256="f" * 64, tags=["unknown-tag"], signature=None),
        ],
    }
    items = map_recent_to_feed_items(response, since_ms=None, limit=10)
    by_sha = {it["sha256"]: it for it in items}
    assert by_sha["d" * 64]["severity"] == 95
    assert by_sha["d" * 64]["threat_family"] == "Hydra"
    assert by_sha["e" * 64]["severity"] == 80
    assert by_sha["e" * 64]["threat_family"] is None
    # Fallback severity must still be recognisable to the on-device decision engine.
    assert by_sha["f" * 64]["severity"] == 70


def test_map_recent_filters_by_since_cursor() -> None:
    older = "2023-01-01 00:00:00"
    newer = "2024-12-31 23:59:00"
    response = {
        "query_status": "ok",
        "data": [
            _entry(sha256="a" * 64, tlsh="T1" + "A" * 70, first_seen=older),
            _entry(sha256="b" * 64, tlsh="T1" + "B" * 70, first_seen=newer),
        ],
    }
    cursor = _epoch_ms(2024, 1, 1)
    items = map_recent_to_feed_items(response, since_ms=cursor, limit=10)
    assert {it["sha256"] for it in items} == {"b" * 64}


def test_map_recent_caps_at_limit_after_filtering() -> None:
    data = [
        _entry(sha256=str(i).rjust(64, "0"), tlsh="T1" + format(i, "X").rjust(70, "0"))
        for i in range(50)
    ]
    response = {"query_status": "ok", "data": data}
    items = map_recent_to_feed_items(response, since_ms=None, limit=5)
    assert len(items) == 5


def test_map_recent_handles_non_ok_status_and_garbage_payload() -> None:
    assert map_recent_to_feed_items({"query_status": "no_results"}, since_ms=None, limit=10) == []
    assert map_recent_to_feed_items({"query_status": "ok"}, since_ms=None, limit=10) == []
    assert (
        map_recent_to_feed_items({"query_status": "ok", "data": "not a list"}, since_ms=None, limit=10)
        == []
    )


def test_build_threat_feed_response_advances_cursor_on_empty_feed() -> None:
    payload = build_threat_feed_response([], requested_limit=200)
    assert payload["items"] == []
    assert payload["next_cursor_ms"] > 0
    assert payload["has_more"] is False


def test_build_threat_feed_response_uses_max_first_seen_as_cursor() -> None:
    items = [
        {"sha256": "a" * 64, "fuzzy_hash": "F" * 70, "first_seen_ms": 100, "severity": 90, "threat_name": "x"},
        {"sha256": "b" * 64, "fuzzy_hash": "F" * 70, "first_seen_ms": 200, "severity": 90, "threat_name": "y"},
    ]
    payload = build_threat_feed_response(items, requested_limit=200)
    assert payload["next_cursor_ms"] == 200
    assert payload["has_more"] is False
    payload_full = build_threat_feed_response(items, requested_limit=2)
    assert payload_full["has_more"] is True


def test_threat_feed_endpoint_mock_mode_returns_empty(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.setenv("TI_MODE", "mock")
    monkeypatch.delenv("MALWAREBAZAAR_AUTH_KEY", raising=False)
    monkeypatch.setenv("THREAT_FEED_RATE_LIMIT_MAX_REQUESTS", "1000")

    import importlib

    import main

    importlib.reload(main)
    client = TestClient(main.app)

    r = client.get("/v1/threat-feed")
    assert r.status_code == 200
    body = r.json()
    assert body["items"] == []
    assert body["has_more"] is False
    assert body["next_cursor_ms"] > 0


def test_threat_feed_endpoint_provider_path(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.setenv("TI_MODE", "malwarebazaar")
    monkeypatch.setenv("MALWAREBAZAAR_AUTH_KEY", "test-key")
    monkeypatch.setenv("THREAT_FEED_RATE_LIMIT_MAX_REQUESTS", "1000")

    import importlib

    import main

    importlib.reload(main)

    fake = {
        "query_status": "ok",
        "data": [
            _entry(sha256="a" * 64, tlsh="T1" + "A" * 70, signature="Cerberus", tags=["banker"]),
            _entry(sha256="b" * 64, tlsh="T1" + "B" * 70, signature=None, tags=["adware"]),
        ],
    }
    with patch.object(main, "fetch_recent_apks_with_cache", return_value=fake):
        client = TestClient(main.app)
        r = client.get("/v1/threat-feed?limit=200")

    assert r.status_code == 200
    body = r.json()
    shas = {it["sha256"] for it in body["items"]}
    assert shas == {"a" * 64, "b" * 64}
    severities = {it["sha256"]: it["severity"] for it in body["items"]}
    assert severities["a" * 64] == 95
    assert severities["b" * 64] == 80
    assert body["next_cursor_ms"] > 0


def test_threat_feed_endpoint_rejects_negative_since(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.setenv("TI_MODE", "mock")
    monkeypatch.setenv("THREAT_FEED_RATE_LIMIT_MAX_REQUESTS", "1000")

    import importlib

    import main

    importlib.reload(main)
    client = TestClient(main.app)
    r = client.get("/v1/threat-feed?since=-1")
    assert r.status_code == 400


def test_threat_feed_endpoint_rate_limit(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.setenv("TI_MODE", "mock")
    monkeypatch.setenv("THREAT_FEED_RATE_LIMIT_WINDOW_SECONDS", "60")
    monkeypatch.setenv("THREAT_FEED_RATE_LIMIT_MAX_REQUESTS", "1")

    import importlib

    import main

    importlib.reload(main)
    client = TestClient(main.app)

    r1 = client.get("/v1/threat-feed")
    r2 = client.get("/v1/threat-feed")
    assert r1.status_code == 200
    assert r2.status_code == 429
    assert "Retry-After" in r2.headers


def test_threat_feed_endpoint_caps_limit_at_max(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.setenv("TI_MODE", "malwarebazaar")
    monkeypatch.setenv("MALWAREBAZAAR_AUTH_KEY", "test-key")
    monkeypatch.setenv("THREAT_FEED_RATE_LIMIT_MAX_REQUESTS", "1000")

    import importlib

    import main

    importlib.reload(main)

    captured: dict = {}

    def _fake(_auth, _cache, *, limit):
        captured["limit"] = limit
        return {"query_status": "ok", "data": []}

    with patch.object(main, "fetch_recent_apks_with_cache", side_effect=_fake):
        client = TestClient(main.app)
        r = client.get(f"/v1/threat-feed?limit={THREAT_FEED_MAX_LIMIT * 10}")

    assert r.status_code == 200
    # Server must clamp to the documented ceiling regardless of what the client requests.
    assert captured["limit"] == THREAT_FEED_MAX_LIMIT
