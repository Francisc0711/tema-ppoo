---
task: file-persistence
artifact: critique
round: 1
plan_version: 1
verdict: approved   # approved | changes-requested
---

# Plan critique: file-persistence, round 1

## Checklist

| Check | Result | Notes |
| --- | --- | --- |
| Every requirement in `scope.md` is covered | pass | R1 Step 1/T1, T9; R2 T2; R3 T3; R4 T4; R5 T5; R6 T6; R7 T7, T8; R8 Step 5 + manual check |
| One logical increment; existing behavior keeps working | pass | only constructors added; existing constructors keep their checks via shared private static methods |
| Within the increment budget (files, lines, dependencies) | pass | 10 production files (at the limit); own estimate ≈ 60–70 model, ≈ 50 codec, ≈ 140–160 storage, ≈ 25 exception + `Main` → 275–300 lines; no dependency. Tight, see suggestions |
| Testable: essential tests mapped to requirements, commands given | pass | 9 new tests, `mvn test`, `@TempDir`; R8 checked manually (console output) |
| Concise: under about 200 lines, snippets only for critical parts | pass | 201 lines; snippets only for record parsing, error wrapping and `Main` |
| Follows `docs/standards/` (rule IDs) | pass | OOP-1 fields private, no setters; OOP-3 try-with-resources; OOP-4 text files, `ArrayList` repository; TEST-3/6/7; GIT-4 `data/` ignored (verified in `.gitignore`) |
| Consistent with `docs/ARCHITECTURE.md` | pass | new `ebanking.storage` package; repository stays free of I/O; history via `addToHistory` (D6) |
| No over-engineering | pass | no interface for a single storage implementation, no options; `CsvCodec` is a small package-private helper |
| Placeholders recorded, with what removes them | pass | TD-app-foundation-1 stays; non-atomic write recorded as `TD-file-persistence-1` → crash-safe-save |
| Snippets correct and secure | pass | `readRecord` handles `""`, quoted commas and multi-line fields (`i = -1; continue` → next index 0); `DateTimeParseException`, `NumberFormatException`, `Enum.valueOf` all caught; `orElseThrow()` safe because `parseTransaction` resolves accounts first; fixed file names, no path from data |
| Risks identified | pass | constructor duplication, line numbering, ISO date round-trip, TD-app-foundation-F1 |

Verified against code: `BankEntity` only constructor generates id/createdAt; `Account` uses `validateOwnerName` (shared with `setOwnerName`, keep it); `Transaction`, `TransferTransaction` checks as described; `BankRepository.addAccount` rejects duplicate numbers, `findAccountById` returns `Optional`, `getAccounts()`/`getTransactions()` copies in insertion order; Java 17 (`maven.compiler.release`) supports the `instanceof` patterns.

## Required changes

None.

## Suggestions

- `BufferedReader.readLine()` treats `\r\n` and a lone `\r` as line ends, so a field containing `\r` comes back as `\n`. Either strip/normalise `\r` on save or accept it and keep T5 on `\n` only; mention it in `KNOWN_ISSUES` if kept.
- Define an empty file (0 bytes, header missing): treat it like a missing file or fail on line 1; the plan says only "first record must equal the header".
- Duplicate account or transaction ids are not rejected; `findAccountById` would silently pick the first. A one-line check in `readAccounts` would protect integrity (not required by R7).
- The R8 manual check says to copy the files written by T3, but `@TempDir` is deleted after the test; write two small files by hand or run a save from a scratch snippet instead.
- The budget is near both limits; if `CsvBankStorage` grows past ≈ 160 lines, keep the parsing methods short rather than adding files.

## Decision

Approved: development can start.
