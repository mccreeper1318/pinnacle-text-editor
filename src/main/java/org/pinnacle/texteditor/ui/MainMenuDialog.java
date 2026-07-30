package org.pinnacle.texteditor.ui;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.InputMap;
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

final class MainMenuDialog extends JPanel {
    private final List<ChoiceDialog.Choice> choices;
    private final List<JLabel> labels = new ArrayList<>();
    private int selectedIndex;

    MainMenuDialog(List<ChoiceDialog.Choice> choices, Runnable closeAction) {
        this.choices = List.copyOf(choices);

        setBackground(RetroTheme.BLACK);
        setBorder(RetroTheme.dialogBorder());
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(620, 470));
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        JLabel title = new JLabel("PINNACLE TEXT EDITOR", SwingConstants.CENTER);
        title.setForeground(RetroTheme.WHITE);
        title.setFont(RetroTheme.MONO_BOLD);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("MAIN MENU", SwingConstants.CENTER);
        subtitle.setForeground(RetroTheme.DIM);
        subtitle.setFont(RetroTheme.MONO_SMALL);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(title);
        add(Box.createVerticalStrut(4));
        add(subtitle);
        add(Box.createVerticalStrut(22));

        for (int index = 0; index < this.choices.size(); index++) {
            final int choiceIndex = index;
            JLabel label = new JLabel(this.choices.get(index).label(), SwingConstants.CENTER);
            label.setFont(RetroTheme.MONO);
            label.setOpaque(true);
            label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            label.setMaximumSize(new Dimension(460, 40));
            label.setPreferredSize(new Dimension(460, 40));
            label.setBorder(BorderFactory.createEmptyBorder(5, 18, 5, 18));
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent event) {
                    selectedIndex = choiceIndex;
                    refreshSelection();
                }

                @Override
                public void mousePressed(MouseEvent event) {
                    selectedIndex = choiceIndex;
                    refreshSelection();
                }

                @Override
                public void mouseClicked(MouseEvent event) {
                    selectedIndex = choiceIndex;
                    choose();
                }
            });
            labels.add(label);
            add(label);
            if (index < this.choices.size() - 1) {
                add(Box.createVerticalStrut(6));
            }
        }

        add(Box.createVerticalGlue());

        JLabel help = new JLabel("↑/↓ select   Enter confirm   Esc close   Mouse supported", SwingConstants.CENTER);
        help.setForeground(RetroTheme.DIM);
        help.setFont(RetroTheme.MONO_SMALL);
        help.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(help);

        installKeyBindings(closeAction);
        refreshSelection();
    }

    private void installKeyBindings(Runnable closeAction) {
        InputMap input = getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actions = getActionMap();

        bind(input, actions, "UP", KeyEvent.VK_UP, () -> move(-1));
        bind(input, actions, "DOWN", KeyEvent.VK_DOWN, () -> move(1));
        bind(input, actions, "LEFT", KeyEvent.VK_LEFT, () -> move(-1));
        bind(input, actions, "RIGHT", KeyEvent.VK_RIGHT, () -> move(1));
        bind(input, actions, "ENTER", KeyEvent.VK_ENTER, this::choose);
        bind(input, actions, "ESCAPE", KeyEvent.VK_ESCAPE, closeAction);
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
        refreshSelection();
    }

    private void choose() {
        choices.get(selectedIndex).action().run();
    }

    private void refreshSelection() {
        for (int index = 0; index < labels.size(); index++) {
            JLabel label = labels.get(index);
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
