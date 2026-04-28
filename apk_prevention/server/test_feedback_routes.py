"""
Tests for the Phase 3.2 privacy-preserving scan feedback endpoint.

Coverage targets:
  * `extra="forbid"` rejects any field the client shouldn't be sending (path / filename).
  * Every field validator: sha256 length, verdict whitelist, layer-key regex, score range,
    rule-name regex, batch size cap, clock-skew window.
  * Per-event "from-the-future" rejection without failing the whole batch.
  * Sink receives the validated event payload only — no raw request body, no extra fields.
  * Pluggable sink: tests inject a fake via `app.dependency_overrides` so we can assert on
    what would actually get persisted in production.
"""
from __future__ import annotations

import time

import pytest
from fastapi.testclient import TestClient

from feedback_routes import (
    ALLOWED_VERDICTS,
    FeedbackSink,
    MAX_EVENTS_PER_BATCH,
    MAX_LAYERS_PER_EVENT,
    MAX_RULES_PER_EVENT,
    get_feedback_sink,
)
from main import app


@pytest.fixture()
def fresh_sink() -> FeedbackSink:
    """Replaces the singleton sink for the duration of a test so assertions don't get
    polluted by other tests' events. Cleared explicitly because the singleton lives for
    the whole test session otherwise."""
    sink = FeedbackSink(capacity=200)
    app.dependency_overrides[get_feedback_sink] = lambda: sink
    yield sink
    app.dependency_overrides.pop(get_feedback_sink, None)


@pytest.fixture()
def client(fresh_sink: FeedbackSink) -> TestClient:
    return TestClient(app)


def _now_ms() -> int:
    return int(time.time() * 1000)


def _valid_event(**overrides) -> dict:
    base = {
        "id": "00000000-0000-0000-0000-000000000001",
        "created_at_ms": _now_ms() - 1000,
        "sha256": "a" * 64,
        "verdict": "SAFE",
        "confidence": 0.92,
        "package_name": "com.example.app",
        "version_code": 17,
        "layer_scores": {"layer1": 0.0, "layer2": 0.5, "layer7": 1.0},
        "triggered_rules": ["banker_anatsa_v1"],
    }
    base.update(overrides)
    return base


def _valid_request(**overrides) -> dict:
    base = {
        "events": [_valid_event()],
        "client_app_version_code": 42,
        "client_android_api_level": 33,
        "uploaded_at_ms": _now_ms(),
    }
    base.update(overrides)
    return base


def test_happy_path_round_trip(client: TestClient, fresh_sink: FeedbackSink) -> None:
    resp = client.post("/v1/feedback", json=_valid_request())
    assert resp.status_code == 200, resp.text
    body = resp.json()
    assert body["accepted_ids"] == ["00000000-0000-0000-0000-000000000001"]
    assert body["rejected_count"] == 0
    snapshot = fresh_sink.snapshot()
    assert len(snapshot) == 1
    persisted = snapshot[0]
    assert persisted["client_app_version_code"] == 42
    assert "received_at_ms" in persisted
    assert persisted["event"]["sha256"] == "a" * 64
    assert "file_path" not in persisted["event"]


def test_rejects_unknown_top_level_field(client: TestClient) -> None:
    payload = _valid_request()
    payload["events"][0]["file_path"] = "/storage/emulated/0/Download/evil.apk"
    resp = client.post("/v1/feedback", json=payload)
    assert resp.status_code in (400, 422), resp.text


def test_rejects_unknown_request_envelope_field(client: TestClient) -> None:
    payload = _valid_request()
    payload["device_imei"] = "490154203237518"
    resp = client.post("/v1/feedback", json=payload)
    assert resp.status_code in (400, 422), resp.text


@pytest.mark.parametrize(
    "field,bad_value",
    [
        ("sha256", "deadbeef"),
        ("sha256", "Z" * 64),
        ("verdict", "ALLOWED"),
        ("confidence", 1.5),
        ("confidence", -0.1),
        ("package_name", "com.evil package"),
        ("package_name", "com/evil"),
        ("version_code", -1),
    ],
)
def test_rejects_malformed_event(client: TestClient, field: str, bad_value) -> None:
    event = _valid_event(**{field: bad_value})
    resp = client.post("/v1/feedback", json=_valid_request(events=[event]))
    assert resp.status_code in (400, 422), resp.text


def test_layer_scores_must_be_in_range(client: TestClient) -> None:
    event = _valid_event(layer_scores={"layer1": 1.2})
    resp = client.post("/v1/feedback", json=_valid_request(events=[event]))
    assert resp.status_code in (400, 422)


def test_layer_keys_are_alnum_underscore_only(client: TestClient) -> None:
    event = _valid_event(layer_scores={"layer 1": 0.5})
    resp = client.post("/v1/feedback", json=_valid_request(events=[event]))
    assert resp.status_code in (400, 422)


def test_rule_names_are_alnum_punct_only(client: TestClient) -> None:
    event = _valid_event(triggered_rules=["evil; DROP TABLE rules--"])
    resp = client.post("/v1/feedback", json=_valid_request(events=[event]))
    assert resp.status_code in (400, 422)


def test_too_many_layers_rejected(client: TestClient) -> None:
    layers = {f"l{i}": 0.0 for i in range(MAX_LAYERS_PER_EVENT + 1)}
    event = _valid_event(layer_scores=layers)
    resp = client.post("/v1/feedback", json=_valid_request(events=[event]))
    assert resp.status_code in (400, 422)


def test_too_many_rules_rejected(client: TestClient) -> None:
    rules = [f"rule_{i}" for i in range(MAX_RULES_PER_EVENT + 1)]
    event = _valid_event(triggered_rules=rules)
    resp = client.post("/v1/feedback", json=_valid_request(events=[event]))
    assert resp.status_code in (400, 422)


def test_too_many_events_in_batch_rejected(client: TestClient) -> None:
    events = [_valid_event(id=f"id-{i:04d}") for i in range(MAX_EVENTS_PER_BATCH + 1)]
    resp = client.post("/v1/feedback", json=_valid_request(events=events))
    assert resp.status_code in (400, 422)


def test_clock_skew_outside_window_rejected(client: TestClient) -> None:
    skewed = _valid_request(uploaded_at_ms=_now_ms() + 365 * 24 * 3600 * 1000)
    resp = client.post("/v1/feedback", json=skewed)
    assert resp.status_code in (400, 422)


def test_per_event_future_timestamp_is_dropped_not_failing_batch(
    client: TestClient, fresh_sink: FeedbackSink
) -> None:
    now = _now_ms()
    good = _valid_event(id="good", created_at_ms=now - 1000)
    future = _valid_event(id="future", created_at_ms=now + 60_000)
    resp = client.post(
        "/v1/feedback",
        json=_valid_request(events=[good, future], uploaded_at_ms=now),
    )
    assert resp.status_code == 200, resp.text
    body = resp.json()
    assert body["accepted_ids"] == ["good"]
    assert body["rejected_count"] == 1
    persisted = fresh_sink.snapshot()
    assert {row["event"]["id"] for row in persisted} == {"good"}


def test_all_events_rejected_returns_400(client: TestClient, fresh_sink: FeedbackSink) -> None:
    now = _now_ms()
    future_only = _valid_event(id="future", created_at_ms=now + 60_000)
    resp = client.post(
        "/v1/feedback",
        json=_valid_request(events=[future_only], uploaded_at_ms=now),
    )
    assert resp.status_code == 400


def test_all_known_verdicts_round_trip(client: TestClient) -> None:
    for verdict in sorted(ALLOWED_VERDICTS):
        resp = client.post(
            "/v1/feedback",
            json=_valid_request(events=[_valid_event(id=verdict, verdict=verdict)]),
        )
        assert resp.status_code == 200, f"{verdict}: {resp.text}"


def test_omitted_optional_package_name_is_accepted(
    client: TestClient, fresh_sink: FeedbackSink
) -> None:
    event = _valid_event(package_name=None, version_code=None)
    resp = client.post("/v1/feedback", json=_valid_request(events=[event]))
    assert resp.status_code == 200, resp.text
    persisted = fresh_sink.snapshot()[0]["event"]
    assert persisted["package_name"] is None
    assert persisted["version_code"] is None


def test_empty_event_list_rejected(client: TestClient) -> None:
    resp = client.post("/v1/feedback", json=_valid_request(events=[]))
    assert resp.status_code in (400, 422)
