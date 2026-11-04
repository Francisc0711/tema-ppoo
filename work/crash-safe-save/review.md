---
task: crash-safe-save
artifact: review
round: 1
plan_version: 1
reviewed_commit: fb31fae
verdict: approved
---

# Code review: crash-safe-save, round 1

## Reviewed

- `git diff main...fb31fae`: 2 production/test files (+ task docs), +167 / -7 lines (production about 52 lines)
- build: ok (via `mvn test`); test: 43 passed, 0 failed; lint: none

## Increments

| Increment | Commit | Plan step or finding | Notes |
| --- | --- | --- | --- |
| 1 | `806684b` | 1. Split writing from moving in save | conforms; `finally` removes both temporary files, error messages still name the failing file; TODO removed. See F1 |
| 2 | `46fae8b` | 2. Atomic move with fallback, behind a tiny seam | conforms; `FileMover` and the constructor are package-private |

## Findings

| ID | Severity | Location | Finding | Rule | Required change |
| --- | --- | --- | --- | --- | --- |
| F1 | nit | `CsvBankStorageTest.java` (T1 to T3) | Fully qualified names (`java.util.Arrays`, `java.nio.file.StandardCopyOption`, ...) instead of imports | none | Use imports; optional |
| F2 | nit | `CsvBankStorage.java:49` | Package-private constructor does not null-check `mover` | none | Optional `Objects.requireNonNull`; only tests call it |

## Previous findings

First round, none.

## Tech debt

- Every placeholder has a `TODO(<id>)` and a row in `tech-debt.md`: yes (TD-crash-safe-save-1 is a documented limitation with no code placeholder; TD-file-persistence-1 resolved and its TODO removed)
- Accepted as debt in this review: F1, F2

## Decision

approved: continue with task-testing. Behaviour matches the plan: temporary files are written first, targets are only touched after both writes succeed, OOP-3 (try-with-resources) holds, no leftovers.
