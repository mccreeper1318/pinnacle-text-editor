package org.pinnacle.texteditor.ui;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.Component;
import java.awt.Dimension;

final class BusyDialog extends JPanel {
    BusyDialog(String message) {
        setBackground(RetroTheme.BLACK);
        setBorder(RetroTheme.dialogBorder());
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(700, 150));
        setFocusable(true);

        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setForeground(RetroTheme.WHITE);
        messageLabel.setFont(RetroTheme.MONO_BOLD);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(Box.createVerticalGlue());
        add(messageLabel);
        add(Box.createVerticalGlue());
    }
}
