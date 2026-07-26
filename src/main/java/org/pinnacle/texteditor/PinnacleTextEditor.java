package org.pinnacle.texteditor;

import org.pinnacle.texteditor.ui.EditorWindow;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;
import java.net.URI;
import java.nio.file.Path;

public final class PinnacleTextEditor {
    private PinnacleTextEditor() {
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        Path initialFile = resolveInitialFile(args);
        SwingUtilities.invokeLater(() -> {
            configureSwingDefaults();
            new EditorWindow(initialFile).open();
        });
    }

    private static Path resolveInitialFile(String[] args) {
        if (args.length == 0 || args[0] == null || args[0].isBlank()) {
            return null;
        }

        String argument = args[0].trim();
        try {
            if (argument.regionMatches(true, 0, "file:", 0, 5)) {
                return Path.of(URI.create(argument));
            }
            return Path.of(argument);
        } catch (RuntimeException exception) {
            System.err.println("Unable to understand the requested file path: " + argument);
            exception.printStackTrace(System.err);
            return null;
        }
    }

    private static void configureSwingDefaults() {
        UIManager.put("Panel.background", Color.BLACK);
        UIManager.put("Label.foreground", Color.WHITE);
        UIManager.put("List.background", Color.BLACK);
        UIManager.put("List.foreground", Color.WHITE);
        UIManager.put("TextField.background", Color.BLACK);
        UIManager.put("TextField.foreground", Color.WHITE);
        UIManager.put("TextField.caretForeground", Color.WHITE);
        UIManager.put("TextArea.background", Color.BLACK);
        UIManager.put("TextArea.foreground", Color.WHITE);
        UIManager.put("TextArea.caretForeground", Color.WHITE);
    }
}
