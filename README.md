# Pinnacle Text Editor

Pinnacle Text Editor is a fullscreen, distraction-free plain-text editor inspired by classic keyboard-driven word processors.

## Install on Linux Mint

The recommended installer is the `.deb` package:

```text
pinnacle-text-editor_0.2.1_amd64.deb
```

1. Double-click the `.deb` file.
2. Open it with Software Manager or Package Installer.
3. Select **Install** and enter the administrator password when asked.
4. Launch **Pinnacle Text Editor** from the application menu.

The installer includes a private Java runtime. Java does not need to be installed separately.

On Linux Mint Cinnamon, the application can be placed on the desktop by finding it in the application menu, right-clicking it, and choosing **Add to desktop**.

## Current features

- Starts in true borderless fullscreen mode.
- Black editing screen with white monospaced text and a blinking caret.
- No title bar, menu bar, toolbar, status bar, or visible scrollbars.
- Text wraps only when it reaches the right edge of the screen.
- Supports normal editing, selection, copy/paste, arrow keys, Home, End, Page Up, Page Down, Backspace, and Delete.
- Reads and writes UTF-8 `.txt` files only.
- Opens associated `.txt` files when launched from the file manager.
- All editor dialogs and file browsers are custom Swing interfaces displayed inside the program.
- Warns before discarding unsaved changes.
- Adds `.txt` automatically when saving a filename without an extension.
- Confirms before overwriting an existing file.
- Checks for updates from inside the editor shortly after startup.
- Supports a manual update check with F5.
- Downloads and SHA-256 verifies update packages before installing them.
- Uses Linux's normal administrator-password prompt to install an accepted update.
- Relaunches the editor after an update is installed.

## Keyboard controls

| Key | Action |
|---|---|
| F1 | Quit |
| F2 | Save |
| F3 | Open a `.txt` document |
| F4 | Create a new document |
| F5 | Check for updates |
| Arrow keys | Navigate text, choices, and file lists |
| Enter | Confirm a dialog choice or open the selected item |
| Escape | Cancel the active dialog |
| Tab | Switch between the folder list and filename field while saving |
| Backspace | Move to the parent folder in a file browser |

## How updates work

There is no separate launcher. Pinnacle Text Editor itself contacts the configured GitHub Releases page after startup. When a newer release exists, the editor asks whether it should be installed.

When accepted, the editor:

1. Downloads the new `.deb` package.
2. Verifies the SHA-256 digest supplied by GitHub.
3. Offers to save unsaved work.
4. Starts the normal Linux privileged package installer.
5. Closes and relaunches itself after installation.

The default update repository is:

```text
mccreeper1318/pinnacle-text-editor
```

The updater will become operational after this project is published to that repository and at least one GitHub Release contains the generated `.deb` file.

## Publishing future updates

A GitHub Actions workflow is included at:

```text
.github/workflows/release-linux.yml
```

For each release:

1. Change `version` in `build.gradle.kts`, such as `0.3.0`.
2. Commit and push the changes.
3. Create and push a matching tag:

```bash
git tag v0.3.0
git push origin v0.3.0
```

The workflow builds the `.deb`, creates the GitHub Release, and uploads the installer. Installed copies will detect it on their next update check.

To use a different GitHub repository, build with:

```bash
./gradlew clean packageDeb -PupdateRepository=OWNER/REPOSITORY
```

## Build requirements

- Java Development Kit 21
- Linux with `jpackage`, `dpkg`, `dpkg-deb`, and `fakeroot`

The included lightweight Gradle bootstrap downloads and checksum-verifies Gradle automatically when needed.

## Run from source

```bash
./gradlew run
```

## Build the Linux installer

```bash
./gradlew clean packageDeb
```

The installer is generated in:

```text
build/jpackage/dist/
```


## Linux Mint dependency compatibility

The Gradle `packageDeb` task automatically adjusts the generated package so the bundled Java runtime can use either Ubuntu/Linux Mint's `libjpeg62` package or Debian's `libjpeg62-turbo` package.
