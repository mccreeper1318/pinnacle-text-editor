package org.pinnacle.texteditor.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

public final class TextFileService {
    private static final Charset WINDOWS_1252 = Charset.forName("windows-1252");

    public String read(Path path) throws IOException {
        Path normalized = requireTextFile(path);
        byte[] bytes = Files.readAllBytes(normalized);

        if (startsWith(bytes, 0xEF, 0xBB, 0xBF)) {
            return StandardCharsets.UTF_8.decode(ByteBuffer.wrap(bytes, 3, bytes.length - 3)).toString();
        }
        if (startsWith(bytes, 0xFF, 0xFE)) {
            return StandardCharsets.UTF_16LE.decode(ByteBuffer.wrap(bytes, 2, bytes.length - 2)).toString();
        }
        if (startsWith(bytes, 0xFE, 0xFF)) {
            return StandardCharsets.UTF_16BE.decode(ByteBuffer.wrap(bytes, 2, bytes.length - 2)).toString();
        }

        try {
            return decodeStrict(bytes, StandardCharsets.UTF_8);
        } catch (CharacterCodingException ignored) {
            // A large number of existing Windows-created .txt files use Windows-1252.
            // It maps every byte, so this fallback lets those documents open instead
            // of failing with a generic UTF-8 decoding error.
            return WINDOWS_1252.decode(ByteBuffer.wrap(bytes)).toString();
        }
    }

    public void write(Path path, String text) throws IOException {
        Path normalized = ensureTxtExtension(path).toAbsolutePath().normalize();
        Path parent = normalized.getParent();
        if (parent == null) {
            throw new IOException("The selected save location has no parent folder.");
        }

        Files.createDirectories(parent);
        if (!Files.isDirectory(parent)) {
            throw new IOException("The selected save location is not a folder.");
        }
        if (!Files.isWritable(parent)) {
            throw new IOException("The selected folder is not writable.");
        }

        String fileName = normalized.getFileName().toString();
        Path temporary = Files.createTempFile(parent, "." + fileName + ".", ".tmp");
        boolean moved = false;
        try {
            Files.writeString(
                    temporary,
                    text,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );

            try {
                Files.move(
                        temporary,
                        normalized,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING
                );
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, normalized, StandardCopyOption.REPLACE_EXISTING);
            }
            moved = true;

            if (!Files.isRegularFile(normalized) || !Files.isReadable(normalized)) {
                throw new IOException("The file was written but could not be verified.");
            }
        } finally {
            if (!moved) {
                Files.deleteIfExists(temporary);
            }
        }
    }

    public Path ensureTxtExtension(Path path) {
        String fileName = path.getFileName().toString();
        if (fileName.toLowerCase().endsWith(".txt")) {
            return path;
        }
        return path.resolveSibling(fileName + ".txt");
    }

    public boolean isTextFile(Path path) {
        if (path == null || path.getFileName() == null) {
            return false;
        }
        return Files.isRegularFile(path)
                && Files.isReadable(path)
                && path.getFileName().toString().toLowerCase().endsWith(".txt");
    }

    private Path requireTextFile(Path path) throws IOException {
        if (path == null) {
            throw new IOException("No file was selected.");
        }
        Path normalized = path.toAbsolutePath().normalize();
        if (normalized.getFileName() == null
                || !normalized.getFileName().toString().toLowerCase().endsWith(".txt")) {
            throw new IOException("Only .txt files can be opened.");
        }
        if (!Files.exists(normalized)) {
            throw new IOException("The selected file does not exist.");
        }
        if (!Files.isRegularFile(normalized)) {
            throw new IOException("The selected path is not a regular file.");
        }
        if (!Files.isReadable(normalized)) {
            throw new IOException("The selected file is not readable.");
        }
        return normalized;
    }

    private String decodeStrict(byte[] bytes, Charset charset) throws CharacterCodingException {
        CharsetDecoder decoder = charset.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        CharBuffer decoded = decoder.decode(ByteBuffer.wrap(bytes));
        return decoded.toString();
    }

    private boolean startsWith(byte[] bytes, int... prefix) {
        if (bytes.length < prefix.length) {
            return false;
        }
        for (int index = 0; index < prefix.length; index++) {
            if ((bytes[index] & 0xFF) != prefix[index]) {
                return false;
            }
        }
        return true;
    }
}
