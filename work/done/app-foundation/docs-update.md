---
task: app-foundation
artifact: docs-update
pr: https://github.com/Francisc0711/tema-ppoo/pull/1
---

# Documentation update: app-foundation

| Document | Change | Source |
| --- | --- | --- |
| `README.md` | rewritten for the Java e-banking app (was the old Node.js Task API): features, JDK 17/Maven requirements, setup, run, test | `scope.md`, `plan.md`, `dev-log.md` |
| `docs/ARCHITECTURE.md` | rewritten for Java: components, data model, decisions D1–D6 (replaces the Node.js decisions) | `plan.md`, `critique.md` |
| `docs/ROADMAP.md` | item 1 `app-foundation` done with PR #1; follow-ups are already roadmap items 2–4 | `scope.md`, `pr.md` |
| `docs/TECH_DEBT.md` | added TD-app-foundation-1, TD-app-foundation-F1, TD-app-foundation-F2 | `tech-debt.md`, `review.md` |
| `docs/KNOWN_ISSUES.md` | unchanged: no known issues to record | `test-report.md` |

## Proposed standards changes

- `docs/standards/git.md` still mentions `node_modules`, `package-lock.json` and `npm ci`; replace with the Maven equivalents (`target/` ignored, versions pinned in `pom.xml`).
- `docs/standards/testing.md` keeps IDs TEST-3, 4, 6, 7 only; consider renumbering or noting the removed IDs.

The task files move to `work/done/app-foundation/` in the same commit.
