# Known issues

Bugs and limitations that are known but not fixed. Each item names the task in which it was found.

| ID | Description | Severity | Found in | Status |
| --- | --- | --- | --- | --- |
| KI-file-persistence-1 | A file truncated inside a quoted last field can load a wrong value (unterminated quote not detected; TD-file-persistence-F1) | minor | file-persistence | open |
| KI-file-persistence-2 | Duplicate ids are not rejected on load; a lone `\r` in a field returns as `\n` (TD-file-persistence-2) | minor | file-persistence | open |
| KI-file-persistence-3 | The restore constructor reports "initial balance" wording for a stored balance (TD-file-persistence-F2) | nit | file-persistence | open |
| KI-file-persistence-4 | The error text for a bad number is the raw `BigDecimal` message | nit | file-persistence | open |
| KI-transaction-operations-1 | `Account.rebuildHistory` has no unit test for more than 10 matching transactions (review F2); checked exploratorily and works | minor | transaction-operations | open |
| KI-transaction-operations-2 | A failed `save` leaves memory ahead of disk; the files stay intact since crash-safe-save (follow-up `save-failure-rollback`) | minor | transaction-operations | open |
| KI-cli-menu-1 | A broken stdin (`UncheckedIOException`) ends the program with a stack trace (review F3) | minor | cli-menu | open |
| KI-cli-menu-2 | The transaction listing prints the raw ISO timestamp with microseconds | nit | cli-menu | open |
| KI-crash-safe-save-1 | A crash between the two moves can leave `accounts.csv` and `transactions.csv` out of sync (TD-crash-safe-save-1) | minor | crash-safe-save | open |
| KI-crash-safe-save-2 | A failed save leaves a blocked temp path in place (only when something non-removable sits at `<file>.tmp`) | trivial | crash-safe-save | open |
