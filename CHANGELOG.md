# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.6.0-beta] - 2026-02-21

### Added
- ** Hourly Chime**: Configurable chime sound on the hour with adjustable volume.
- **Sleep Timer**: Countdown timer to automatically close the app.
- **App Shortcuts**: Added shortcuts for Dark/Light mode and Settings.
- **OLED Burn-in Protection**: Subtle pixel shifting with translation compensation.
- **Accessibility**: Clock announces current time; Light button announces state.
- **Internationalization**: Added Simplified Chinese documentation (`README.zh-CN.md`).
- **License**: Transitioned to GPLv3 to support F-Droid submission.

### Changed
- **Settings Sheet**: Improved gesture handling (drag-to-dismiss, scroll synchronization).
- **Power Optimization**: Zero background activity when screen off or app hidden.
- **Audio**: Replaced flip sound and rebalanced audio levels.
- **Rendering**: Cached theme colors and noise shaders for better performance.
- **Gesture Control**: Brightness dimming restricted to single-finger scroll.

### Fixed
- Resolved Sleep Timer crash.
- Fixed Invert button sound feedback.
- Fixed Light overlay rendering to avoid GPU software fallback.
