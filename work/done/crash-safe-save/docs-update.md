---
task: crash-safe-save
artifact: docs-update
pr: https://github.com/Francisc0711/tema-ppoo/pull/5
---

# Documentation update: crash-safe-save

| Document | Change | Source |
| --- | --- | --- |
| `README.md` | Persistence feature row and Run section: save is crash-safe | scope.md, pr.md |
| `docs/ARCHITECTURE.md` | Storage row updated; D16 reworded; D21 to D23 added | plan.md, review.md |
| `docs/ROADMAP.md` | Item 5 crash-safe-save done with PR; follow-ups atomic-pair-save and save-failure-rollback added; old follow-up removed | scope.md |
| `docs/TECH_DEBT.md` | TD-file-persistence-1 resolved; TD-crash-safe-save-1, -F1, -F2 added | tech-debt.md, review.md |
| `docs/KNOWN_ISSUES.md` | KI-transaction-operations-2 reworded; KI-crash-safe-save-1 and -2 added | test-report.md |

## Proposed standards changes

- None

The task files move to `work/done/<slug>/` in the same commit.
