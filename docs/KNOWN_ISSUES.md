# Known issues

Bugs and limitations that are known but not fixed. Each item names the task in which it was found.

| ID | Description | Severity | Found in | Status |
| --- | --- | --- | --- | --- |
| KI-file-persistence-1 | A file truncated inside a quoted last field can load a wrong value (unterminated quote not detected; TD-file-persistence-F1) | minor | file-persistence | open |
| KI-file-persistence-2 | Duplicate ids are not rejected on load; a lone `\r` in a field returns as `\n` (TD-file-persistence-2) | minor | file-persistence | open |
| KI-file-persistence-3 | The restore constructor reports "initial balance" wording for a stored balance (TD-file-persistence-F2) | nit | file-persistence | open |
| KI-file-persistence-4 | The error text for a bad number is the raw `BigDecimal` message | nit | file-persistence | open |
| KI-transaction-operations-1 | `Account.rebuildHistory` has no unit test for more than 10 matching transactions (review F2); checked exploratorily and works | minor | transaction-operations | open |
| KI-transaction-operations-2 | A failed `save` leaves memory ahead of disk (follow-up `crash-safe-save`) | minor | transaction-operations | open |
