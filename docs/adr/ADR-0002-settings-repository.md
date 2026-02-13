# ADR-0002: Settings Repository as Persistence Facade

## Status
Accepted

## Context
Settings reads/writes are spread across UI/controller/viewmodel and tie upper layers to storage details.

## Decision
- Define `SettingsRepository` contract as the only upper-layer entry for migrated settings.
- Keep flow-based outputs from repository for reactive UI.
- Keep DataStore migration as a future implementation swap behind the same contract.

## Consequences
- Upper layers stop depending on `SharedPreferences` API details.
- A future DataStore migration can be done with minimal upstream changes.
- Migration must be done incrementally to keep behavior stable.
