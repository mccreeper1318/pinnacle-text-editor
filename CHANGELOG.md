# Changelog

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
