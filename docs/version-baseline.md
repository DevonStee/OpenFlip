# OpenFlip Version Baseline

This project uses a locked dependency baseline to keep builds stable on the Kotlin `2.0.x` track.

## Source of truth

- App dependency versions: `/Users/black_knife_air/Projects/fliqlo_android/app/build.gradle.kts` (`object Baseline`)
- Plugin versions: `/Users/black_knife_air/Projects/fliqlo_android/build.gradle.kts`

## Policy

1. Do not upgrade dependencies ad hoc.
2. Update baseline only in a dedicated "dependency lane" PR.
3. For each upgrade batch, run:
   - `./gradlew assembleDebug`
   - `./gradlew test`
   - `./gradlew lint`
   - `./gradlew installDebug` + real-device smoke launch
4. If an upgrade pulls Kotlin metadata above `2.0.x`, defer until Kotlin toolchain migration is approved.

## Current compatibility note

- `com.github.skydoves:cloudy` newer releases pull Kotlin metadata beyond `2.0.x` in this environment.
- Baseline remains on `0.2.4` until the project upgrades Kotlin toolchain.

## Last verification snapshot

- Verified against `app/build.gradle.kts` and root `build.gradle.kts` on 2026-02-13.
- Last validated runtime branch snapshot: `main` at commit `5390c8ed`.
