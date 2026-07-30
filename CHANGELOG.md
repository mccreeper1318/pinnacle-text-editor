# Changelog

## 0.3-beta.2

- Changed printed documents to use black text on a white page, independent of the editor's black-screen theme.
- Added **About PTE** to the Esc menu.
- Added an About dialog showing the application name, creator, copyright notice, and current installed version.
- Connected the About dialog version to the Gradle/jpackage release version so it updates automatically with each build.
- Added regression checks for printable background and text colors and About-menu behavior.

## 0.3

- Replaced the F1 through F5 editor actions with a keyboard-controlled Esc main menu.
- Added Save Document, Open Document, New Document, Print Document, Check for Updates, and Exit Program menu choices.
- Added Esc-to-close behavior for the main menu while preserving Esc cancellation in other dialogs.
- Added direct document printing through the system printer dialog.
- Added 0.75-inch print margins and paginated text rendering to prevent bottom-of-page clipping.
- Changed screen wrapping from character wrapping to whole-word wrapping whenever a word fits on the next line.
- Fixed updater version handling for `0.3`, `v0.3`, and `v.0.3` release tag formats.
- Added a release-list fallback when GitHub's latest-release endpoint is unavailable or has no eligible release.
- Added checksum-file support when GitHub does not provide a release asset digest.
- Added detailed update and download failure messages.
- Added dependency-free regression checks for version parsing, print margins, and multi-page pagination.
- Added Build and Test and release packaging workflows adapted from PinDB.

## 0.2.2

- Fixed saving documents to Desktop and other selected folders.
- Added mouse support to confirmation choices, message dialogs, file browsing, and save controls.
- Added visible OPEN, SAVE, CANCEL, and OK controls while retaining full keyboard operation.
- Added double-click support for entering folders and opening `.txt` files.
- Added clear folder-selection instructions inside the open and save dialogs.
- Added a success message showing the exact path after a document is saved.
- Improved save reliability with verified, atomic UTF-8 writes.
- Improved save errors so the selected path and actual failure reason are displayed.
- Added support for opening UTF-8, UTF-8 BOM, UTF-16 LE, UTF-16 BE, and common Windows-1252 `.txt` files.
- Fixed the Linux desktop MIME association so double-clicking a `.txt` file passes the file to Pinnacle Text Editor.
- Added support for both normal paths and `file:` URI arguments from Linux desktop environments.

## 0.2.1

- Fixed Linux Mint and Ubuntu installer compatibility by accepting `libjpeg62` as an alternative to Debian's `libjpeg62-turbo` dependency.

## 0.2.0

- Added a self-contained Linux `.deb` installer.
- Added an application-menu shortcut and `.txt` file association.
- Bundled a private Java runtime.
- Added in-app update checking and installation through GitHub Releases.
- Added F5 manual update checking.
