# Roadmap

Each item is one increment, developed as a task (see `AGENTS.md`). Status: planned or done.

| # | Slug | Increment | Status | Pull request |
| --- | --- | --- | --- | --- |
| 1 | app-foundation | Crearea claselor de bază (Tranzactie, Cont) folosind încapsularea și structuri de date (vectori, ArrayList). | done | https://github.com/Francisc0711/tema-ppoo/pull/1 |
| 2 | file-persistence | Implementarea logicii de citire și scriere a datelor exclusiv în fișiere text/binare. | done | https://github.com/Francisc0711/tema-ppoo/pull/2 |
| 3 | transaction-operations | Logica de procesare: adăugare, actualizare și ștergere tranzacții din memorie și fișiere. | done | https://github.com/Francisc0711/tema-ppoo/pull/3 |
| 4 | cli-menu | O interfață simplă în consolă pentru ca utilizatorul să interacționeze cu sistemul de e-banking. | done | https://github.com/Francisc0711/tema-ppoo/pull/4 |

## Follow-ups

Items found while working on other tasks, not yet scheduled.

| Slug | Description | Found in |
| --- | --- | --- |
| crash-safe-save | Write to a temp file in the same directory, then move it over the old file atomically (removes TD-file-persistence-1) | file-persistence |
