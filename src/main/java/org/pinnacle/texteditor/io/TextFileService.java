package org.pinnacle.texteditor.io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class TextFileService {
    public String read(Path path) throws IOException {
        requireTextFile(path);
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    public void write(Path path, String text) throws IOException {
        Path normalized = ensureTxtExtension(path).toAbsolutePath().normalize();
        Path parent = normalized.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        Files.writeString(
                normalized,
                text,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        );
    }

    public Path ensureTxtExtension(Path path) {
        String fileName = path.getFileName().toString();
        if (fileName.toLowerCase().endsWith(".txt")) {
            return path;
        }
        return path.resolveSibling(fileName + ".txt");
    }

    public boolean isTextFile(Path path) {
        return Files.isRegularFile(path)
                && path.getFileName().toString().toLowerCase().endsWith(".txt");
    }

    private void requireTextFile(Path path) {
        if (!isTextFile(path)) {
            throw new IllegalArgumentException("Only existing .txt files can be opened.");
        }
    }
}
