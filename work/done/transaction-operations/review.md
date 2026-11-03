---
task: transaction-operations
artifact: review
round: 1
plan_version: 1
reviewed_commit: 57b6858
verdict: approved
---

# Code review: transaction-operations, round 1

## Reviewed

- `git diff main...57b6858`: 14 files (7 production, 2 test, 5 task files), +721 / -1 lines
- build: success; test: 28 passed, 0 failed; lint: none (AGENTS.md)

## Increments

| Increment | Commit | Plan step or finding | Notes |
| --- | --- | --- | --- |
| 1 | `62c7e8d` | 1. Account | conforms; funds checked before mutation, involvement check closes TD-app-foundation-F1 |
| 2 | `29e0af4` | 2. Transaction copy with a new amount | conforms; id, createdAt and accounts kept, covariant returns |
| 3 | `6e37599` | 3. Repository | conforms; `replaceTransaction` keeps the position |
| 4 | `8ffe030` | 4. TransactionService | conforms; all funds checks precede mutations, save only after success; see F1 |

## Findings

| ID | Severity | Location | Finding | Rule | Required change |
| --- | --- | --- | --- | --- | --- |
| F1 | nit | `work/transaction-operations/dev-log.md` (increment 4) | Commit given as "see git log" instead of the hash `8ffe030` | - | Optional: write the hash |
| F2 | minor | `AccountTest` | `rebuildHistory` is not tested with more than 10 matching transactions (ring wrap-around after a rebuild) | TEST-6 | Optional: add a test; may be accepted as debt |

## Previous findings

First round.

## Tech debt

- Every placeholder has a `TODO(<id>)` and a row in `tech-debt.md`: yes (no placeholders; none introduced)
- Accepted as debt in this review: none (F1 and F2 are optional, non-blocking)

## Decision

approved: continue with task-testing.
