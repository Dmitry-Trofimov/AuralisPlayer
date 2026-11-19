# Changelog

Notable changes are documented here.

## 1.2.0 — project optimization

- Removed local build output, Gradle caches and IDE-specific files from the distributable source archive.
- Enabled R8 and resource shrinking for Release builds.
- Removed unused dependencies from the app module.
- Replaced the image loading dependency with local Android bitmap decoding and a bounded artwork cache.
- Preserved the Liquid Glass UI, local library, background playback and equalizer implementation.

## 1.1.1

- Fixed nullable launch intent handling when creating the MediaSession activity PendingIntent.

## 1.1.0

- Added the 10-point equalizer UI and genre presets.
- Updated the app typography.
