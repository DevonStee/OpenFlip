# ADR-0004: Consolidate Fullscreen Clock Runtime into Feature Package

## Status
Accepted

## Context
Fullscreen clock classes were spread across `controller/`, `ui/`, `view/`, and parts of `manager/` in `:app`, making feature ownership and navigation expensive.

## Decision
- Consolidate fullscreen clock runtime/UI classes under `com.bokehforu.openflip.feature.clock`.
- Keep existing implementation logic unchanged; apply package/path refactor only.
- Move clock-specific managers (`TimeProvider`, `TimeSecondsTicker`, `DisplayBurnInProtectionManager`, `LightEffectManager`) under `feature.clock.manager`.
- Update manifest/XML class references to new package paths.

## Consequences
- Clock feature boundaries are explicit and cohesive.
- Churn for future clock-specific refactors is reduced.
- Tests and DI imports must follow the new package paths.
