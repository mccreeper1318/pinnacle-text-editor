package org.pinnacle.texteditor.ui;

import java.nio.file.Path;

record FileEntry(Path path, EntryType type) {
    enum EntryType {
        PARENT,
        DIRECTORY,
        TEXT_FILE
    }

    String displayName() {
        return switch (type) {
            case PARENT -> "[..]";
            case DIRECTORY -> "[" + path.getFileName() + "]";
            case TEXT_FILE -> path.getFileName().toString();
        };
    }
}
