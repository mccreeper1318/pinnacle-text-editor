package org.pinnacle.texteditor.ui;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

final class ChoiceDialog extends JPanel {
    record Choice(String label, Runnable action) {
    }

    private final List<Choice> choices;
    private final List<JLabel> choiceLabels = new ArrayList<>();
    private int selectedIndex;

    ChoiceDialog(String message, List<Choice> choices, Runnable cancelAction) {
        this.choices = List.copyOf(choices);
        setBackground(RetroTheme.BLACK);
        setBorder(RetroTheme.dialogBorder());
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(700, 190));
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setForeground(RetroTheme.WHITE);
        messageLabel.setFont(RetroTheme.MONO_BOLD);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel choicesPanel = new JPanel();
        choicesPanel.setBackground(RetroTheme.BLACK);
        choicesPanel.setLayout(new BoxLayout(choicesPanel, BoxLayout.X_AXIS));
        choicesPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        for (int index = 0; index < this.choices.size(); index++) {
            final int choiceIndex = index;
            JLabel label = new JLabel(this.choices.get(index).label(), SwingConstants.CENTER);
            label.setFont(RetroTheme.MONO);
            label.setOpaque(true);
            label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            label.setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 14));
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent event) {
                    selectedIndex = choiceIndex;
                    refreshChoices();
                }

                @Override
                public void mousePressed(MouseEvent event) {
                    selectedIndex = choiceIndex;
                    refreshChoices();
                }

                @Override
                public void mouseClicked(MouseEvent event) {
                    selectedIndex = choiceIndex;
                    choose();
                }
            });
            choiceLabels.add(label);
            choicesPanel.add(label);
            if (index < this.choices.size() - 1) {
                choicesPanel.add(Box.createHorizontalStrut(20));
            }
        }

        JLabel help = new JLabel("←/→ select   Enter confirm   Esc cancel   Mouse supported", SwingConstants.CENTER);
        help.setForeground(RetroTheme.DIM);
        help.setFont(RetroTheme.MONO_SMALL);
        help.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(messageLabel);
        add(Box.createVerticalStrut(28));
        add(choicesPanel);
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
        for (int index = 0; index < choiceLabels.size(); index++) {
            JLabel label = choiceLabels.get(index);
            if (index == selectedIndex) {
                label.setBackground(RetroTheme.SELECTED_BACKGROUND);
                label.setForeground(RetroTheme.SELECTED_FOREGROUND);
            } else {
                label.setBackground(RetroTheme.BLACK);
                label.setForeground(RetroTheme.WHITE);
            }
        }
    }
}
