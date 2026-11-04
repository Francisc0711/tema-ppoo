---
task: cli-menu
artifact: critique
round: 1
plan_version: 1
verdict: approved
---

# Plan critique: cli-menu, round 1

## Checklist

| Check | Result | Notes |
| --- | --- | --- |
| Every requirement in `scope.md` is covered | pass | R1-R9 each map to a menu choice or `Main` step and to T1-T11 |
| One logical increment; existing behavior keeps working | pass | one new method in the service, new UI layer, `Main` rewired; no model or format change |
| Within the increment budget (files, lines, dependencies) | pass | 3 production files, about 230 lines (my estimate: 200-250), no dependency |
| Testable: essential tests mapped to requirements, commands given | pass | `mvn test`; R2/`Main` wiring has a manual command |
| Concise: under about 200 lines, snippets only for critical parts | pass | about 150 lines, two small snippets |
| Follows `docs/standards/` (rule IDs) | pass | OOP-1, OOP-3 (stdin not closed, justified), TEST-3/4/7 |
| Consistent with `docs/ARCHITECTURE.md` | pass | rules stay in the service (D12); the menu only resolves numbers to ids; ARCHITECTURE needs updating in task-doc-updates |
| No over-engineering | pass | one class, no menu-item abstraction; T-count is one per requirement |
| Placeholders recorded, with what removes them | pass | none; resolves TD-app-foundation-1 |
| Snippets correct and secure | pass | verified against the code (see below) |
| Risks identified | pass | memory ahead of disk (KI-transaction-operations-2), corrupt file on start |

Claims checked against the code:
- `Account(String, String, BigDecimal, AccountType)` validates and throws `ValidationException`; `addAccount` rejects duplicates; `save` calls `createDirectories`, so a missing `data/` works (R2, T3).
- `ValidationException` (checked) and `InsufficientFundsException` (unchecked, extends `RuntimeException`) are unrelated, so the multi-catch compiles.
- `findTransactionsByAccount` uses `involves`, so transfers appear for both accounts; list order is file order.
- `getRecentHistory()` returns a trimmed copy (size = `historySize`, no nulls), oldest first; reversing it gives newest first.
- `TransferTransaction` rejects source = target, so a self-transfer from the menu gives a clean error.
- `CsvBankStorage.load()` returns an empty repository for missing files.

## Required changes

| ID | Plan section | Problem | Required change |
| --- | --- | --- | --- |
| none | | | |

## Suggestions

- T10 bundles four error cases in one test; separate scripted runs inside it are fine, but keep assertions per case so a failure names the case.
- Add `ValidationException`-wrapped parsing for the transaction number (non-integer input) explicitly in `readTransaction`; the plan only names the range check.
- Mention in the plan that `describe` prints the date via `getCreatedAt()` (exists in `BankEntity`) to avoid a surprise during development.

## Decision

Approved: development can start.
