package org.pinnacle.texteditor.ui;

import javax.swing.BorderFactory;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Font;

final class RetroTheme {
    static final Color BLACK = Color.BLACK;
    static final Color WHITE = new Color(235, 235, 235);
    static final Color DIM = new Color(150, 150, 150);
    static final Color SELECTED_BACKGROUND = WHITE;
    static final Color SELECTED_FOREGROUND = BLACK;
    static final Font MONO = new Font(Font.MONOSPACED, Font.PLAIN, 20);
    static final Font MONO_SMALL = new Font(Font.MONOSPACED, Font.PLAIN, 17);
    static final Font MONO_BOLD = new Font(Font.MONOSPACED, Font.BOLD, 18);

    private RetroTheme() {
    }

    static Border dialogBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(WHITE, 2),
                BorderFactory.createEmptyBorder(18, 24, 18, 24)
        );
    }
}
