---
task: cli-menu
artifact: pr
branch: cli-menu
base: main
updated_commit: none
pr: not created
---

# feat(cli-menu): add console menu

## Summary

Adds an interactive console menu so the user can manage accounts and transactions with `mvn compile exec:java`. It replaces the placeholder `Main` and resolves TD-app-foundation-1.

## Changes

- `ebanking.cli.ConsoleMenu` (new): menu over `InputStream`/`PrintStream`. It creates and lists accounts; deposits, withdraws and transfers; lists, updates and deletes transactions by number; shows the last 10. Errors print one line and return to the menu.
- `TransactionService.createAccount`: validated, duplicate-checked and saved account creation.
- `Main`: loads `data/` (empty if missing), wires storage, repository, service and menu; stops on a corrupt data file.
- Tests: `ConsoleMenuTest` (T1-T10), `TransactionServiceTest` (T11).

## How to test

```bash
mvn clean install -DskipTests
mvn test
```

Manual: `printf '2\n0\n' | mvn -q compile exec:java` in a directory without `data/` prints the menu, "No accounts." and exits. 40 tests pass; exploratory checks E1-E4 pass.

## Quality gates

| Gate | Task file | Result |
| --- | --- | --- |
| Plan critique | `critique.md`, round 1 | approved |
| Code review | `review.md`, round 1 | approved |
| Tests | `test-report.md`, round 1 | pass |

## Tech debt

- None (resolves TD-app-foundation-1)

## Follow-ups

- crash-safe-save: atomic file writes (already on the roadmap)
