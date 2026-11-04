---
task: crash-safe-save
artifact: critique
round: 1
plan_version: 1
verdict: approved
---

# Plan critique: crash-safe-save, round 1

## Checklist

| Check | Result | Notes |
| --- | --- | --- |
| Every requirement in `scope.md` is covered | pass | R1 to T1, R2 to T2, R3 to T3, R4 to T4 (existing tests) |
| One logical increment; existing behavior keeps working | pass | only `save` changes; format, `load` and public API unchanged; the existing tests call only the public constructor |
| Within the increment budget (files, lines, dependencies) | pass | 1 production file, about 40 lines, no dependency (my estimate agrees) |
| Testable: essential tests mapped to requirements, commands given | pass | three new tests, `mvn test` |
| Concise: under about 200 lines, snippets only for critical parts | pass | about 110 lines, two short snippets |
| Follows `docs/standards/` (rule IDs) | pass | OOP-3 (try-with-resources kept in the write methods), OOP-4 (File I/O), TEST-4, TEST-7 (no mock library) |
| Consistent with `docs/ARCHITECTURE.md` | pass | D16 stays valid; plan hands its update to task-doc-updates |
| No over-engineering | pass | the package-private `FileMover` is the smallest way to test R3 without a mocking library; no other options or layers |
| Placeholders recorded, with what removes them | pass | none in code; `TD-crash-safe-save-1` for the two-file gap, resolves `TD-file-persistence-1` |
| Snippets correct and secure | pass | see notes below |
| Risks identified | pass | fixed temp names, non-atomic file systems, pair gap |

## Verification against the code

- `save` currently truncates through `open(...)` and carries `TODO(TD-file-persistence-1)`: matches the plan's context.
- T2 works: `Files.newBufferedWriter` on an existing non-empty directory throws `IOException`; `deleteQuietly` on that directory ignores `DirectoryNotEmptyException`; `accounts.csv.tmp` is removed by `finally`.
- `finally` after successful moves is harmless (`deleteIfExists` on a missing file).
- `new AtomicMoveNotSupportedException(source, target, reason)` is constructible in T3.

## Required changes

None.

## Suggestions

- In T2, assert the old content with `Files.readAllBytes` before and after, as the plan says, and also that `transactions.csv` is unchanged (the plan already lists both files).
- Make `ATOMIC_MOVE` the only option on the first attempt exactly as shown; do not add `REPLACE_EXISTING` to it (on some platforms combining them is ignored, but it blurs the intent).
- Add the missing imports (`CopyOption`, `StandardCopyOption`, `AtomicMoveNotSupportedException`) when implementing.

## Decision

approved: development can start.
