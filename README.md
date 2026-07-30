# Pinnacle Text Editor

Pinnacle Text Editor (PTE) is a fullscreen, distraction-free plain-text editor for Linux. It is inspired by classic keyboard-driven word processors while supporting modern file handling, printing, mouse controls, and in-app updates.

**Current development release:** `0.3-beta.2`

> Beta releases may contain unfinished features or defects. Back up important documents before testing a beta build.

## Features

- Borderless fullscreen editing environment.
- Black editing screen with white monospaced text and a blinking caret.
- Whole-word wrapping at the right edge of the screen.
- Esc-key menu for all document and application actions.
- Keyboard and mouse support throughout menus and file dialogs.
- Opens UTF-8, UTF-8 BOM, UTF-16 LE, UTF-16 BE, and common Windows-1252 `.txt` files.
- Saves documents as UTF-8 plain text.
- Opens associated `.txt` files directly from the Linux file manager.
- Warns before unsaved changes are discarded.
- Automatically adds the `.txt` extension when needed.
- Confirms before overwriting an existing file.
- Prints documents as normal black text on a white page.
- Uses safe print margins, whole-word wrapping, and automatic multi-page pagination.
- Checks GitHub Releases for updates from inside the editor.
- Downloads and verifies update packages before installation.
- Includes a self-contained Java runtime in the Linux `.deb` package.

## Supported systems

The packaged application is intended for 64-bit Debian-based Linux distributions, including:

- Linux Mint
- Ubuntu
- Debian

The installer bundles its own Java runtime, so Java does not need to be installed separately.

## Installing a release

1. Open the repository's **Releases** page.
2. Download the `.deb` package for the desired version.
3. Optionally download the matching `.sha256` file and verify it:

   ```bash
   sha256sum -c pinnacle-text-editor_*.deb.sha256
   ```

4. Install the package by double-clicking it and opening it with Software Manager or Package Installer.

The package can also be installed from a terminal:

```bash
sudo apt install ./pinnacle-text-editor_*.deb
```

After installation, launch **Pinnacle Text Editor** from the desktop application menu.

On Linux Mint Cinnamon, the application can be added to the desktop by right-clicking it in the application menu and selecting **Add to desktop**.

## Using Pinnacle Text Editor

Press **Esc** while editing to open the main menu.

| Menu item | Action |
|---|---|
| Save Document | Saves the current document or asks where a new document should be saved. |
| Open Document | Opens the built-in browser for selecting an existing `.txt` file. |
| New Document | Clears the editor after protecting any unsaved changes. |
| Print Document | Opens the system printer dialog and prints black text on a white page. |
| Check for Updates | Checks the configured GitHub Releases page for a newer version. |
| About PTE | Displays the application name, creator, copyright notice, and installed version. |
| Exit Program | Closes the editor after protecting any unsaved changes. |

Press **Esc** again to close the menu without selecting anything.

### General controls

| Control | Action |
|---|---|
| Arrow keys | Navigate text, menu choices, and file lists. |
| Enter | Confirm the selected choice or open the selected item. |
| Esc | Open or close the main menu, or cancel the active dialog. |
| Tab | Move between controls in the save dialog. |
| Backspace | Move to the parent folder in a file browser. |
| Mouse click | Select menu choices and dialog controls. |
| Double-click | Enter a folder or open a selected `.txt` file. |
| Alt+F4 | Request to exit the application. |

## Printing

PTE uses the system print service and printer dialog. Printed documents use:

- Black text on a white page
- 0.75-inch safe margins
- Whole-word wrapping
- Automatic page breaks
- Multi-page pagination that prevents text from being cut off at the bottom

A printer must already be configured in the Linux system settings.

## In-app updates

PTE does not use a separate update launcher. The editor itself checks the configured GitHub repository for published releases.

When an update is accepted, PTE:

1. Downloads the new `.deb` package.
2. Verifies the package checksum.
3. Offers to save unsaved work.
4. Opens the normal Linux administrator-password prompt.
5. Installs the package and relaunches the editor.

Manual update checks are available through **Esc → Check for Updates**.

## Building from source

### Requirements

- Linux
- Java Development Kit 21
- `jpackage`
- `dpkg` and `dpkg-deb`
- `fakeroot`
- Git

On Ubuntu or Linux Mint, the packaging tools can be installed with:

```bash
sudo apt update
sudo apt install openjdk-21-jdk fakeroot dpkg-dev git
```

### Clone and verify

```bash
git clone https://github.com/mccreeper1318/pinnacle-text-editor.git
cd pinnacle-text-editor
./gradlew --no-daemon clean check
```

The included Gradle bootstrap downloads and checksum-verifies the required Gradle distribution when necessary.

### Run from source

```bash
./gradlew run
```

### Build the application distribution

```bash
./gradlew installDist
```

The runnable distribution is generated under:

```text
build/install/pinnacle-text-editor/
```

### Build the Linux installer

```bash
./gradlew clean check packageDeb
```

The `.deb` installer is generated under:

```text
build/jpackage/dist/
```

To build a specific application version:

```bash
./gradlew clean check packageDeb -PappVersion=0.3-beta.2
```

To use a different repository for in-app update checks:

```bash
./gradlew clean packageDeb -PupdateRepository=OWNER/REPOSITORY
```

## Automated builds

The repository includes GitHub Actions workflows for:

- Compiling and verifying pushes and pull requests.
- Building the application distribution and Debian package.
- Uploading CI package artifacts.
- Building published releases.
- Generating SHA-256 checksum files.
- Uploading installers and checksums to GitHub Releases.

Release tags may use formats such as `0.3`, `v0.3`, or `v.0.3`. Prerelease versions such as `0.3-beta.2` are packaged using Debian-compatible version ordering.

## License

Pinnacle Text Editor is available under the [MIT License](LICENSE).
