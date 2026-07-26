package org.pinnacle.texteditor.ui;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

final class MessageDialog extends JPanel {
    MessageDialog(String message, Runnable closeAction) {
        setBackground(RetroTheme.BLACK);
        setBorder(RetroTheme.dialogBorder());
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(700, 180));
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setForeground(RetroTheme.WHITE);
        messageLabel.setFont(RetroTheme.MONO_BOLD);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel help = new JLabel("Press Enter or Esc", SwingConstants.CENTER);
        help.setForeground(RetroTheme.DIM);
        help.setFont(RetroTheme.MONO_SMALL);
        help.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(messageLabel);
        add(Box.createVerticalGlue());
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
