# Architecture Baseline (OpenFlip)

## Scope
- Keep current UI behavior and interaction semantics unchanged.
- Keep module dependencies unidirectional and explicit.
- Keep Android entry points and feature orchestration discoverable by package.

## Current Modules and Responsibilities
- `:app`: Android entry points, manifest-registered components, custom views, app-level DI composition.
- `:core`: shared contracts, models, and cross-layer utilities.
- `:domain`: use cases and repository/gateway contracts.
- `:data`: repository implementations and persistence (`AppSettingsManager`).
- `:feature-clock`: clock feature state/lifecycle logic.
- `:feature-settings`: settings feature UI and controllers.

## In-App Feature Package Organization
- Clock runtime/UI lives under `feature-clock/src/main/java/com/bokehforu/openflip/feature/clock/`.
- Clock-specific managers live under `feature-clock/src/main/java/com/bokehforu/openflip/feature/clock/manager/`.
- Settings feature UI and ViewModel live under `feature-settings/src/main/java/com/bokehforu/openflip/feature/settings/`.
- Manifest entry points in `:app` must reference the feature package names (`.feature.clock.*`, `.feature.chime.*`) where applicable.

## Dependency Direction
- `feature-settings/ui` -> `feature-settings/viewmodel` -> `domain/usecase` -> `domain/repository` -> `data/repository` -> `data/settings(AppSettingsManager)`.
- `feature-clock/controller` consumes `SettingsRepository.settingsFlow` and applies diffs to runtime UI/controllers.
- `:app` composes `:core`, `:domain`, `:data`, `:feature-clock`, `:feature-settings`.
- Avoid direct `SharedPreferences` access outside `AppSettingsManager`.

## Baseline Stability Rules
- Keep dependency baseline pinned unless a dedicated dependency PR is approved.
- Every architecture change must pass:
  - `./gradlew :app:compileDebugKotlin`
  - `./gradlew test`
  - `./gradlew lint`
  - `./gradlew installDebug`

## Non-Goals (This Wave)
- No visual redesign.
- No behavioral re-spec for gestures/animations.
- No forced DataStore migration.
