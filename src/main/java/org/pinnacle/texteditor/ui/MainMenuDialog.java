package org.pinnacle.texteditor.ui;

import org.pinnacle.texteditor.AppInfo;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
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
    private static final String ABOUT_LABEL = "About PTE";

    private final List<ChoiceDialog.Choice> choices;
    private final List<JLabel> labels = new ArrayList<>();
    private final Runnable closeAction;
    private int selectedIndex;
    private boolean aboutVisible;

    MainMenuDialog(List<ChoiceDialog.Choice> choices, Runnable closeAction) {
        this.closeAction = closeAction;
        this.choices = withAboutChoice(choices);

        setBackground(RetroTheme.BLACK);
        setBorder(RetroTheme.dialogBorder());
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(620, 530));
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        installKeyBindings();
        showMenu();
    }

    private List<ChoiceDialog.Choice> withAboutChoice(List<ChoiceDialog.Choice> suppliedChoices) {
        List<ChoiceDialog.Choice> menuChoices = new ArrayList<>(suppliedChoices);
        ChoiceDialog.Choice about = new ChoiceDialog.Choice(ABOUT_LABEL, this::showAbout);

        int exitIndex = -1;
        for (int index = 0; index < menuChoices.size(); index++) {
            if ("Exit Program".equals(menuChoices.get(index).label())) {
                exitIndex = index;
                break;
            }
        }

        if (exitIndex >= 0) {
            menuChoices.add(exitIndex, about);
        } else {
            menuChoices.add(about);
        }
        return List.copyOf(menuChoices);
    }

    private void showMenu() {
        aboutVisible = false;
        labels.clear();
        removeAll();

        JLabel title = centeredLabel("PINNACLE TEXT EDITOR", RetroTheme.MONO_BOLD, RetroTheme.WHITE);
        JLabel subtitle = centeredLabel("MAIN MENU", RetroTheme.MONO_SMALL, RetroTheme.DIM);

        add(title);
        add(Box.createVerticalStrut(4));
        add(subtitle);
        add(Box.createVerticalStrut(22));

        for (int index = 0; index < choices.size(); index++) {
            final int choiceIndex = index;
            JLabel label = new JLabel(choices.get(index).label(), SwingConstants.CENTER);
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
            if (index < choices.size() - 1) {
                add(Box.createVerticalStrut(6));
            }
        }

        add(Box.createVerticalGlue());
        add(centeredLabel(
                "↑/↓ select   Enter confirm   Esc close   Mouse supported",
                RetroTheme.MONO_SMALL,
                RetroTheme.DIM
        ));

        selectedIndex = Math.min(selectedIndex, choices.size() - 1);
        refreshSelection();
        refreshPanel();
    }

    private void showAbout() {
        aboutVisible = true;
        labels.clear();
        removeAll();

        add(centeredLabel("ABOUT PTE", RetroTheme.MONO_BOLD, RetroTheme.WHITE));
        add(Box.createVerticalStrut(30));

        for (String line : aboutLines()) {
            add(centeredLabel(line, RetroTheme.MONO, RetroTheme.WHITE));
            add(Box.createVerticalStrut(12));
        }

        add(Box.createVerticalGlue());

        JButton backButton = new JButton("OK");
        backButton.setBackground(RetroTheme.BLACK);
        backButton.setForeground(RetroTheme.WHITE);
        backButton.setFont(RetroTheme.MONO);
        backButton.setBorder(BorderFactory.createLineBorder(RetroTheme.WHITE, 1));
        backButton.setFocusPainted(false);
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.addActionListener(event -> showMenu());
        add(backButton);
        add(Box.createVerticalStrut(14));
        add(centeredLabel("Press Enter or Esc, or click OK", RetroTheme.MONO_SMALL, RetroTheme.DIM));

        refreshPanel();
    }

    static List<String> aboutLines() {
        return List.of(
                "Pinnacle Text Editor",
                "Created by: McCreeper1318",
                "(C) All rights reserved",
                "Version: PTE " + AppInfo.VERSION
        );
    }

    boolean isAboutVisibleForTest() {
        return aboutVisible;
    }

    private JLabel centeredLabel(String text, java.awt.Font font, java.awt.Color color) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(color);
        label.setFont(font);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private void refreshPanel() {
        revalidate();
        repaint();
        requestFocusInWindow();
    }

    private void installKeyBindings() {
        InputMap input = getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actions = getActionMap();

        bind(input, actions, "UP", KeyEvent.VK_UP, () -> move(-1));
        bind(input, actions, "DOWN", KeyEvent.VK_DOWN, () -> move(1));
        bind(input, actions, "LEFT", KeyEvent.VK_LEFT, () -> move(-1));
        bind(input, actions, "RIGHT", KeyEvent.VK_RIGHT, () -> move(1));
        bind(input, actions, "ENTER", KeyEvent.VK_ENTER, this::choose);
        bind(input, actions, "ESCAPE", KeyEvent.VK_ESCAPE, this::handleEscape);
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
        if (aboutVisible) {
            return;
        }
        selectedIndex = Math.floorMod(selectedIndex + amount, choices.size());
        refreshSelection();
    }

    private void choose() {
        if (aboutVisible) {
            showMenu();
            return;
        }
        choices.get(selectedIndex).action().run();
    }

    private void handleEscape() {
        if (aboutVisible) {
            showMenu();
        } else {
            closeAction.run();
        }
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
