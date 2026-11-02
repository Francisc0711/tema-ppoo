---
task: app-foundation
artifact: review
round: 1
plan_version: 1
reviewed_commit: 58191d4    # HEAD when the review started
verdict: approved   # approved | changes-requested
---

# Code review: app-foundation, round 1

## Reviewed

- `git diff main...58191d4`: 23 files, +921 / -50 lines (code: 17 Java files and `pom.xml`, `.gitignore`; the rest are task files and the human-owned `docs/standards/testing.md`)
- build: `mvn compile` success; test: 9 passed, 0 failed; lint: none (AGENTS.md)

## Increments

| Increment | Commit | Plan step or finding | Notes |
| --- | --- | --- | --- |
| 1 | `3c5d82b` | 1. Build setup | conforms; versions as planned, `TODO(TD-app-foundation-1)` present |
| 2 | `2fc00d4` | 2. Exceptions, 3. BankEntity and enums | conforms; merging two small steps is justified |
| 3 | `0b974bc` | 4. Transaction hierarchy | conforms; added getters justified by OOP-1; `involves()` override is correct polymorphism (OOP-2); T5 move is justified |
| 4 | `b8e3a48` | 5. Account | conforms; buffer arithmetic is correct (oldest index moves only when full, copy returned oldest first); `final balance` is a sound choice for now |
| 5 | `1dc164b` | 6. BankRepository, 7. Tests | conforms; duplicate check runs before `add`; read methods return copies |

## Findings

| ID | Severity | Location | Finding | Rule | Required change |
| --- | --- | --- | --- | --- | --- |
| F1 | minor | `src/main/java/ebanking/model/Account.java:62` | `addToHistory` accepts a transaction that does not involve this account (`!transaction.involves(getId())`), so an account history can hold another account's transactions | — | In `transaction-operations`, when the history is wired up, reject transactions that do not involve the account (`ValidationException`) and add a test |
| F2 | nit | `src/test/java/ebanking/model/TransactionTest.java:5` | The model-package test depends on `BankRepository` for T5 (critique suggestion); the model tests are no longer independent of the repository | TEST-7 | None required; you could move the type-filter part of T5 to `BankRepositoryTest` later |

## Previous findings

Not applicable (round 1).

## Tech debt

- Every placeholder has a `TODO(<id>)` and a row in `tech-debt.md`: yes (`TD-app-foundation-1`, `Main.java:10`)
- Accepted as debt in this review: F1 (for `transaction-operations`), F2

## Decision

Approved: continue with task-testing.
