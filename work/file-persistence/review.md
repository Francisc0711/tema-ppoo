---
task: file-persistence
artifact: review
round: 1
plan_version: 1
reviewed_commit: 441400a
verdict: approved
---

# Code review: file-persistence, round 1

## Reviewed

- `git diff main...441400a`: 17 files (12 source and test files), production code about +300 lines, mostly the new storage package
- build: ok; test: 18 passed, 0 failed; lint: none

## Increments

| Increment | Commit | Plan step or finding | Notes |
| --- | --- | --- | --- |
| 1 | `0d45e7a` | 1. Restore constructors | conforms; validation moved to private static methods, normal constructors keep identical checks |
| 2 | `8509dad` | 2-4. Storage package | conforms; `readFile`/`RecordHandler` deviation is justified; see F1, F2 |
| 3 | `bdf0a2b` | 5. Main | conforms; `TODO(TD-app-foundation-1)` kept |
| 4 | `75bfcd5` | 6. Tests | conforms; T1-T9 cover R1-R8 |

## Findings

| ID | Severity | Location | Finding | Rule | Required change |
| --- | --- | --- | --- | --- | --- |
| F1 | minor | `CsvCodec.java:48` | An unterminated quote at end of file ends the record silently. A file truncated inside a quoted last field can still have six fields and load with a wrong value. The comment "field count check fails later" is not always true. | | Throw an `IOException` or a format error for an unterminated quote, or accept as debt. |
| F2 | nit | `Account.java:94` | The restore constructor reports "initial balance must be zero or positive" for a stored balance. | | Neutral wording, optional. |

## Previous findings

First round.

## Tech debt

- Every placeholder has a `TODO(<id>)` and a row in `tech-debt.md`: yes (`TD-file-persistence-1` in `save`; `TD-file-persistence-2` is a limitation without a code marker)
- Accepted as debt in this review: F1 (add to `tech-debt.md` in the next development round, or fix), F2

## Decision

approved: continue with task-testing. No blocker or major finding; resources use try-with-resources (OOP-3), no database (OOP-4), fields private (OOP-1).
