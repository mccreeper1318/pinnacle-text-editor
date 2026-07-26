package org.pinnacle.texteditor.update;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class UpdateInstaller {
    public boolean canInstall() {
        return System.getProperty("os.name", "").toLowerCase().contains("linux")
                && Boolean.getBoolean("pinnacle.packaged");
    }

    public void installAndRestart(Path debPackage) throws IOException {
        if (!canInstall()) {
            throw new IOException("Automatic installation is only available in the installed Linux application");
        }
        if (!Files.isRegularFile(debPackage)) {
            throw new IOException("The downloaded update package is missing");
        }

        String launcher = ProcessHandle.current()
                .info()
                .command()
                .orElse("/opt/pinnacle-text-editor/bin/Pinnacle Text Editor");
        String script = """
                DEB="$1"
                APP="$2"
                if command -v pkexec >/dev/null 2>&1; then
                  if pkexec /usr/bin/apt-get install -y "$DEB"; then
                    rm -f "$DEB"
                  fi
                  nohup "$APP" >/dev/null 2>&1 &
                elif command -v xdg-open >/dev/null 2>&1; then
                  xdg-open "$DEB" >/dev/null 2>&1
                else
                  exit 127
                fi
                """;

        ProcessBuilder installer = new ProcessBuilder(
                "/bin/sh",
                "-c",
                script,
                "pinnacle-text-editor-update",
                debPackage.toAbsolutePath().toString(),
                launcher
        );
        installer.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        installer.redirectError(ProcessBuilder.Redirect.DISCARD);
        installer.start();
    }
}
