---
task: file-persistence
artifact: tech-debt
---

# Tech debt: file-persistence

| ID | Location | Description | Kind | Removed by | Status |
| --- | --- | --- | --- | --- | --- |
| TD-file-persistence-1 | `CsvBankStorage.save` | Files are written in place; a crash mid-save can leave a truncated file | shortcut | crash-safe-save | open |
| TD-file-persistence-2 | `CsvBankStorage` | Duplicate account or transaction ids are not rejected on load; a lone `\r` inside a field comes back as `\n` | limitation | later hardening | open |
