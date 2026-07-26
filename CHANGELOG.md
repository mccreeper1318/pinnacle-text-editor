# Changelog

## 0.2.1

- Fixed Linux Mint and Ubuntu installation failing on the unavailable `libjpeg62-turbo` dependency.
- Debian packages now accept either `libjpeg62` on Ubuntu/Mint or `libjpeg62-turbo` on Debian.
- Added automatic dependency correction to the Gradle packaging task for future releases.

## 0.2.0 - Linux installation and in-app updates

- Added a self-contained Linux `.deb` installer.
- Added a normal **Pinnacle Text Editor** application-menu entry.
- Added an application icon.
- Bundled a private Java 21 runtime so users do not need to install Java separately.
- Added `.txt` file association and command-line file opening.
- Added an automatic in-app update check shortly after startup.
- Added F5 for a manual update check.
- Added GitHub Releases update discovery.
- Added verified `.deb` downloads using the GitHub-provided SHA-256 digest.
- Added unsaved-change protection before update installation.
- Added Linux administrator-password installation through the normal PolicyKit prompt.
- Added automatic relaunch after a successful update.
- Added a GitHub Actions workflow that builds and publishes Linux installers from version tags.
- Added a Gradle `packageDeb` task.

## 0.1.0 - Initial build

- Added borderless fullscreen startup.
- Added a black, distraction-free editing surface with white monospaced text.
- Added a blinking white caret and edge-only character wrapping.
- Added normal multiline text editing and navigation behavior.
- Added F1 keyboard-controlled quit confirmation.
- Added F2 keyboard-controlled save confirmation.
- Added F3 custom in-program `.txt` file browser.
- Added F4 new-document behavior.
- Added custom in-program save browser and filename entry.
- Added UTF-8 `.txt` reading and writing.
- Added automatic `.txt` extension handling.
- Added overwrite confirmation.
- Added unsaved-change protection before quitting, opening, or creating a new document.
- Added Java 21 and Gradle build configuration.
