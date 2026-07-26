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
import java.util.List;
import java.util.function.Consumer;

final class ChoiceDialog extends JPanel {
    record Choice(String label, Runnable action) {
    }

    private final List<Choice> choices;
    private final JLabel choicesLabel;
    private int selectedIndex;

    ChoiceDialog(String message, List<Choice> choices, Runnable cancelAction) {
        this.choices = List.copyOf(choices);
        setBackground(RetroTheme.BLACK);
        setBorder(RetroTheme.dialogBorder());
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(620, 180));
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setForeground(RetroTheme.WHITE);
        messageLabel.setFont(RetroTheme.MONO_BOLD);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        choicesLabel = new JLabel();
        choicesLabel.setFont(RetroTheme.MONO);
        choicesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        choicesLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel help = new JLabel("←/→ select   Enter confirm   Esc cancel", SwingConstants.CENTER);
        help.setForeground(RetroTheme.DIM);
        help.setFont(RetroTheme.MONO_SMALL);
        help.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(messageLabel);
        add(Box.createVerticalStrut(28));
        add(choicesLabel);
        add(Box.createVerticalGlue());
        add(help);

        installKeyBindings(cancelAction);
        refreshChoices();
    }

    private void installKeyBindings(Runnable cancelAction) {
        InputMap input = getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actions = getActionMap();

        bind(input, actions, "LEFT", KeyEvent.VK_LEFT, () -> move(-1));
        bind(input, actions, "RIGHT", KeyEvent.VK_RIGHT, () -> move(1));
        bind(input, actions, "UP", KeyEvent.VK_UP, () -> move(-1));
        bind(input, actions, "DOWN", KeyEvent.VK_DOWN, () -> move(1));
        bind(input, actions, "ENTER", KeyEvent.VK_ENTER, this::choose);
        bind(input, actions, "ESCAPE", KeyEvent.VK_ESCAPE, cancelAction);
    }

    private void bind(InputMap input, ActionMap actions, String name, int keyCode, Runnable action) {
        input.put(KeyStroke.getKeyStroke(keyCode, 0), name);
        actions.put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                action.run();
            }
        });
    }

    private void move(int amount) {
        selectedIndex = Math.floorMod(selectedIndex + amount, choices.size());
        refreshChoices();
    }

    private void choose() {
        choices.get(selectedIndex).action().run();
    }

    private void refreshChoices() {
        StringBuilder text = new StringBuilder("<html>");
        for (int index = 0; index < choices.size(); index++) {
            String label = escapeHtml(choices.get(index).label());
            if (index == selectedIndex) {
                text.append("<span style='background:#EBEBEB;color:#000000'>&nbsp;")
                        .append(label)
                        .append("&nbsp;</span>");
            } else {
                text.append("<span style='color:#EBEBEB'>&nbsp;")
                        .append(label)
                        .append("&nbsp;</span>");
            }
            if (index < choices.size() - 1) {
                text.append("&nbsp;&nbsp;&nbsp;");
            }
        }
        text.append("</html>");
        choicesLabel.setText(text.toString());
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
