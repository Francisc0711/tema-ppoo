---
task: crash-safe-save
artifact: tech-debt
---

# Tech debt: crash-safe-save

| ID | Location | Description | Kind | Removed by | Status |
| --- | --- | --- | --- | --- | --- |
| TD-crash-safe-save-1 | `CsvBankStorage.save` | A crash between the two moves can leave `accounts.csv` and `transactions.csv` out of sync | limitation | atomic-pair-save | open |
| TD-file-persistence-1 | `CsvBankStorage.save` | Truncating in-place write; resolved by this task | shortcut | crash-safe-save | resolved |
