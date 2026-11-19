# Contributing to Auralis Player

Thanks for taking an interest in Auralis Player!

## Before opening an issue

- Search existing Issues to avoid duplicates.
- Include Android version, device model and app version.
- For playback problems, describe the audio format and the steps that reproduce the issue.
- Never attach private audio files, personal data or device identifiers.

## Code changes

1. Fork the repository and create a focused branch.
2. Keep changes small and aligned with the existing Kotlin/Compose structure.
3. Preserve the Liquid Glass design language unless the change specifically concerns UI design.
4. Do not add network services, analytics, tracking or new permissions without documenting why they are needed.
5. Do not commit `local.properties`, build output, APKs, signing keys or passwords.
6. Run Gradle Sync and `assembleDebug` before opening a pull request when possible.
7. Describe the change, the reason for it and how it was tested.

## Pull requests

A pull request should solve one focused problem. Include screenshots or a short recording for visible UI changes, and mention any device-specific limitations.
