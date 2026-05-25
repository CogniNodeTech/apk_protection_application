# SafeGuard Detection-Rate Benchmarking (Phase 3.3)

This document describes how to measure SafeGuard's classification quality against a
labelled corpus, how to reproduce an existing score, and how to publish a new one.

The benchmark is intentionally lightweight: a CSV manifest of `(sha256, label)` rows,
a pluggable scan oracle, and a deterministic JVM harness that prints
precision / recall / F1 / FPR. Any heavier ML-pipeline workflow lives downstream of
this format, not in place of it.

> **Why we're being careful here:** a malware scanner with no published precision/recall
> number is statistically indistinguishable from `Math.random() < 0.5 ? "MALICIOUS" :
> "SAFE"`. Phase 3.3 is what lets us claim a quality bar with evidence.

---

## 1. Quick start

```bash
# Linux/macOS
export SAFEGUARD_BENCHMARK_MANIFEST=/abs/path/to/corpus.csv
export SAFEGUARD_BENCHMARK_ORACLE=/abs/path/to/oracle.json   # optional
export SAFEGUARD_BENCHMARK_RUN_ID=corpus-2026-04-27          # optional

./gradlew :core:testDebugUnitTest \
  --tests "com.safeguard.core.benchmark.CorpusBenchmarkSmokeTest" \
  --rerun-tasks
```

```powershell
# Windows PowerShell
$env:SAFEGUARD_BENCHMARK_MANIFEST = "D:\corpus\smoke.csv"
$env:SAFEGUARD_BENCHMARK_ORACLE   = "D:\corpus\oracle.json"
$env:SAFEGUARD_BENCHMARK_RUN_ID   = "smoke-2026-04-27"

.\gradlew.bat :core:testDebugUnitTest `
  --tests "com.safeguard.core.benchmark.CorpusBenchmarkSmokeTest" `
  --rerun-tasks
```

When the env var is **not** set, the test is **skipped** (treated as passing) — CI
boxes without a corpus stay green.

---

## 2. Corpus manifest format

CSV with a required header row. Required columns: `sha256`, `label`. Optional columns:
`apk_path`, plus any number of curator metadata columns (e.g. `family`, `source`,
`note`) that are folded into the per-row report.

```csv
sha256,label,apk_path,family,source,note
5b1c4e…64,malicious,/data/corpus/anatsa/v3/01.apk,anatsa,vt-2026-03,banker dropper
d29df4…64,clean,,playstore-2024-q4,playstore,bundled-with-pixel
e7ae90…64,SAFE,/data/corpus/clean/wechat-8.0.30.apk,messengers,vendor,
```

**Allowed labels** (case-insensitive):

| Token       | Mapped to            |
|-------------|----------------------|
| `clean`     | `Verdict.SAFE`       |
| `safe`      | `Verdict.SAFE`       |
| `benign`    | `Verdict.SAFE`       |
| `malicious` | `Verdict.MALICIOUS`  |
| `malware`   | `Verdict.MALICIOUS`  |
| `bad`       | `Verdict.MALICIOUS`  |

Anything else is reported on stderr and the row is skipped — corpus authors should fix
the manifest, not have the run abort halfway through.

**Lines starting with `#` are treated as comments**, and blank lines are ignored.
Quoted fields support embedded commas and `""`-escaped quotes (no embedded newlines).

> The corpus is **content-addressed by SHA-256**, not by file path. That means the same
> manifest works on every dev machine — only the optional `apk_path` column changes.
> This is also why we don't ship a stock corpus in the repo: the index can be public,
> the binaries cannot.

---

## 3. The scan oracle

A `ScanOracle` answers the question "given this `(sha256, expected_label)`, what verdict
does SafeGuard's engine emit?". Today we ship two implementations:

1. **`InMemoryScanOracle`** — backed by a `Map<sha256, OracleVerdict>`. Used by the
   smoke harness when `SAFEGUARD_BENCHMARK_ORACLE` points to a JSON map of pre-recorded
   verdicts.
2. **(Roadmap, app:)** A `ScanOrchestrator`-backed oracle that runs the actual layered
   pipeline against the on-disk APKs in `apk_path`. This lives in the `app:` module
   so the `core:` benchmark stays JVM-pure for CI; we'll wire it up in a follow-up
   change once the orchestrator's harness mode is stable.

### Oracle JSON format

```json
{
  "5b1c4e…64": "MALICIOUS",
  "d29df4…64": "SAFE",
  "e7ae90…64": "SAFE"
}
```

Verdict tokens are the names of the `Verdict` enum: `SAFE`, `MALICIOUS`, `SUSPICIOUS`,
`UNKNOWN`. Unknown SHA-256 keys fall back to the configured default (currently
`UNKNOWN`).

> If the oracle file is missing or unreadable, the harness still runs but reports a
> degenerate "everything is UNKNOWN" score. That's intentional — a misconfigured oracle
> should be visible in the metrics, not silenced.

---

## 4. How metrics are computed

The harness models the problem as a binary classifier (positive = `MALICIOUS`):

| Predicted ↓ \ Actual → | `MALICIOUS`              | `SAFE`                   |
|------------------------|--------------------------|--------------------------|
| `MALICIOUS`            | TP                       | FP                       |
| `SAFE`                 | FN                       | TN                       |
| `SUSPICIOUS`           | abstain (recall miss)    | abstain (counted FPR-)   |
| `UNKNOWN`              | abstain (recall miss)    | abstain (counted FPR-)   |

* **Precision** = `TP / (TP + FP)` — of everything we flagged, how much was actually bad.
* **Recall** = `TP / (TP + FN + abstain_on_malicious)` — of everything that *was* bad,
  how much did we flag. **Abstaining counts as a miss** for recall, because from the
  user's perspective an "I'm not sure" verdict on a banker is functionally identical
  to a false negative.
* **F1** = `2 · precision · recall / (precision + recall)`.
* **False-positive rate** = `FP / (TN + FP + abstain_on_safe)` — of clean apps, how
  often did we wrongly call them malicious.
* **Accuracy on committed** = `(TP + TN) / (TP + TN + FP + FN)` — sanity check that
  ignores abstains.

We deliberately do **not** collapse `SUSPICIOUS` into "predicted malicious" for the
metric calculation. Doing so would inflate recall for free at the cost of precision and
obscure the question we actually care about: *is the engine confidently right when it
commits*.

---

## 5. Output format

The harness prints a stable, terminal-friendly summary:

```
=== SafeGuard detection-rate run: smoke-2026-04-27 ===
samples_total            842
samples_committed        798
true_positives           413
false_positives          7
true_negatives           385
false_negatives          12
abstain_on_malicious     19
abstain_on_safe          25
                         
precision                0.9833
recall                   0.9304
f1                       0.9561
false_positive_rate      0.0167
accuracy_on_committed    0.9762
```

**Every key in this block is a stable contract** — downstream dashboards and the
`docs/BENCHMARKING.md#published-scores` table below grep for these literally. If you
need to add a new metric, *append* it; don't rename existing keys.

---

## 6. Publishing a new score

1. Run the harness end-to-end with a deterministic `SAFEGUARD_BENCHMARK_RUN_ID`
   (e.g. `corpus-v3-2026-04-27`).
2. Copy the printed report block.
3. Add a row to the table in §7 below with: run id, corpus name+size, oracle source
   (live `ScanOrchestrator` vs. recorded JSON), precision, recall, F1, FPR, and a
   one-line note about what changed since the previous score (new YARA rule, threat-
   feed cutover, ML model swap, etc.).
4. Open a PR titled `bench: <run-id>` — reviewers should verify that the run id is
   reproducible from the commit being landed (or a previous one that's tagged).

> **Don't** silently overwrite a worse score with a better one without a changelog
> entry. Regressions are useful: a recall drop in the next score after a feature ships
> is a powerful signal that the feature broke detection coverage.

---

## 7. Published scores

| Run ID                  | Date       | Corpus / size                    | Oracle          | Precision | Recall | F1     | FPR    | Notes |
|-------------------------|------------|----------------------------------|-----------------|-----------|--------|--------|--------|-------|
| _example-placeholder_   | 2026-04-27 | smoke (n=4 SAFE, n=4 MALICIOUS)  | InMemory recorded | 1.0000  | 1.0000 | 1.0000 | 0.0000 | Pipeline smoke test only — replace with a real run. |

> **The placeholder row above is intentional.** Replace it with the first reproducible
> corpus run as part of Phase 3 wrap-up. We do not currently publish a "real"
> SafeGuard detection-rate number against a public corpus — we will add one as soon as
> a corpus license that we can ship is in place.

---

## 8. Internals (for `core:` maintainers)

The harness lives entirely under `core/src/main/kotlin/com/safeguard/core/benchmark`
and `core/src/test/kotlin/com/safeguard/core/benchmark`:

* `BenchmarkCorpus.kt` — `BenchmarkSample` data class + `BenchmarkCorpusLoader` parser.
* `BenchmarkMetrics.kt` — pure-function `ConfusionMatrix` math + `renderReport()`.
* `BenchmarkRunner.kt` — `ScanOracle` interface, `InMemoryScanOracle`, the
  single-threaded `BenchmarkRunner`.
* `CorpusBenchmarkSmokeTest.kt` — env-var-gated end-to-end JUnit test that ties
  manifest loading → oracle wiring → runner → report.

The metric, parser, and runner are each independently unit-tested; the smoke harness
is what stitches them together for a real corpus run. When extending the harness:

* **Don't** silently change the `renderReport()` key set — downstream dashboards parse
  these literally. Add new metrics by appending lines.
* **Do** keep the runner single-threaded. Corpus runs are I/O-bound on the APK reader
  and the decision engine is cheap; the simplicity of a deterministic loop wins over
  coroutine-fanout machinery for the size of corpora we expect (a few thousand
  samples at most before someone moves the harness to a real ML pipeline).
* **Do** keep the metric calculator (`BenchmarkMetrics`) pure — no logging, no I/O,
  no system clock. Tests rely on it.
