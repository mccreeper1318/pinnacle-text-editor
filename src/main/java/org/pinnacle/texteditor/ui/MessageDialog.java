package org.pinnacle.texteditor.ui;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

final class MessageDialog extends JPanel {
    MessageDialog(String message, Runnable closeAction) {
        setBackground(RetroTheme.BLACK);
        setBorder(RetroTheme.dialogBorder());
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(900, 240));
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setForeground(RetroTheme.WHITE);
        messageLabel.setFont(RetroTheme.MONO_BOLD);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton closeButton = new JButton("OK");
        closeButton.setBackground(RetroTheme.BLACK);
        closeButton.setForeground(RetroTheme.WHITE);
        closeButton.setFont(RetroTheme.MONO);
        closeButton.setBorder(BorderFactory.createLineBorder(RetroTheme.WHITE, 1));
        closeButton.setFocusPainted(false);
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeButton.addActionListener(event -> closeAction.run());

        JLabel help = new JLabel("Press Enter or Esc, or click OK", SwingConstants.CENTER);
        help.setForeground(RetroTheme.DIM);
        help.setFont(RetroTheme.MONO_SMALL);
        help.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(messageLabel);
        add(Box.createVerticalGlue());
        add(closeButton);
        add(Box.createVerticalStrut(12));
        add(help);

        InputMap input = getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actions = getActionMap();
        input.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "close");
        input.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
        actions.put("close", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                closeAction.run();
            }
        });
    }
}
