# ADR-0001: Layer Boundaries and Dependency Direction

## Status
Accepted

## Context
UI/controller code and persistence details have been coupled, making changes risky and hard to test in isolation.

## Decision
- Introduce stable layer boundaries:
  - `domain/usecase` for business actions.
  - `data/repository` for persistence abstraction.
- Keep `AppSettingsManager` as implementation detail behind repository.
- Enforce dependency direction:
  - `ui/controller/viewmodel` depends on use cases, not on persistence API details.

## Consequences
- Business logic becomes testable without Android components.
- Incremental migration can proceed without UI behavior changes.
- Additional boilerplate (use cases + repository contracts) is accepted.
