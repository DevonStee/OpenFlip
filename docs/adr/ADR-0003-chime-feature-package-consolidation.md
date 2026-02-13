# ADR-0003: Consolidate Hourly Chime Code into Feature Package

## Status
Accepted

## Context
Hourly chime code was spread across `manager/` and `data/repository/` in `:app`, making ownership unclear and increasing search/refactor cost.

## Decision
- Group all hourly chime runtime components into `com.bokehforu.openflip.feature.chime`.
- Group chime gateway adapter implementations into `com.bokehforu.openflip.feature.chime.data`.
- Update Android manifest component paths to `.feature.chime.*`.
- Keep behavior unchanged (package/location-only refactor).

## Consequences
- Chime feature boundaries are now explicit and easier to maintain.
- DI bindings remain unchanged semantically, but imports point to feature package paths.
- Future chime extraction into a dedicated module is simpler because classes are already grouped.
