package org.pinnacle.texteditor.ui;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

final class SaveFileDialog extends JPanel {
    private enum FocusArea {
        DIRECTORIES,
        FILE_NAME
    }

    private final Consumer<Path> saveAction;
    private final Runnable cancelAction;
    private final JLabel pathLabel = new JLabel();
    private final JLabel statusLabel = new JLabel(" ");
    private final DefaultListModel<FileEntry> model = new DefaultListModel<>();
    private final JList<FileEntry> directoryList = new JList<>(model);
    private final JTextField fileNameField = new JTextField("untitled.txt");
    private Path currentDirectory;
    private FocusArea focusArea = FocusArea.FILE_NAME;

    SaveFileDialog(Path initialDirectory, Consumer<Path> saveAction, Runnable cancelAction) {
        super(new BorderLayout(0, 12));
        this.saveAction = saveAction;
        this.cancelAction = cancelAction;
        this.currentDirectory = normalizeInitialDirectory(initialDirectory);

        setBackground(RetroTheme.BLACK);
        setBorder(RetroTheme.dialogBorder());
        setPreferredSize(new Dimension(900, 620));
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        add(createHeader(), BorderLayout.NORTH);
        add(createDirectoryList(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        installBindings();
        loadDirectory();
    }

    @Override
    public boolean requestFocusInWindow() {
        focusArea = FocusArea.FILE_NAME;
        fileNameField.selectAll();
        return fileNameField.requestFocusInWindow();
    }

    private JComponent createHeader() {
        JPanel header = new JPanel();
        header.setBackground(RetroTheme.BLACK);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("SAVE TEXT DOCUMENT");
        title.setForeground(RetroTheme.WHITE);
        title.setFont(RetroTheme.MONO_BOLD);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        pathLabel.setForeground(RetroTheme.DIM);
        pathLabel.setFont(RetroTheme.MONO_SMALL);
        pathLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(8));
        header.add(pathLabel);
        return header;
    }

    private JComponent createDirectoryList() {
        directoryList.setBackground(RetroTheme.BLACK);
        directoryList.setForeground(RetroTheme.WHITE);
        directoryList.setSelectionBackground(RetroTheme.SELECTED_BACKGROUND);
        directoryList.setSelectionForeground(RetroTheme.SELECTED_FOREGROUND);
        directoryList.setFont(RetroTheme.MONO);
        directoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        directoryList.setFixedCellHeight(28);
        directoryList.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        directoryList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list,
                                                          Object value,
                                                          int index,
                                                          boolean isSelected,
                                                          boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                label.setText(((FileEntry) value).displayName());
                label.setFont(RetroTheme.MONO);
                label.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return label;
            }
        });

        directoryList.addListSelectionListener(event -> {
            if (event.getValueIsAdjusting()) {
                return;
            }
            FileEntry selected = directoryList.getSelectedValue();
            if (selected == null) {
                statusLabel.setText(" ");
            } else if (selected.type() == FileEntry.EntryType.TEXT_FILE) {
                statusLabel.setText("Press Enter or double-click to use this existing filename.");
            } else {
                statusLabel.setText("Press Enter or double-click to enter this folder before saving.");
            }
        });

        directoryList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    int index = directoryList.locationToIndex(event.getPoint());
                    if (index >= 0) {
                        directoryList.setSelectedIndex(index);
                        openSelectedEntry();
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(directoryList);
        scroll.setBorder(BorderFactory.createLineBorder(RetroTheme.WHITE, 1));
        scroll.getViewport().setBackground(RetroTheme.BLACK);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    private JComponent createFooter() {
        JPanel footer = new JPanel();
        footer.setBackground(RetroTheme.BLACK);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));

        JPanel fileNameRow = new JPanel(new BorderLayout(12, 0));
        fileNameRow.setBackground(RetroTheme.BLACK);
        JLabel label = new JLabel("Filename:");
        label.setForeground(RetroTheme.WHITE);
        label.setFont(RetroTheme.MONO);

        fileNameField.setBackground(RetroTheme.BLACK);
        fileNameField.setForeground(RetroTheme.WHITE);
        fileNameField.setCaretColor(RetroTheme.WHITE);
        fileNameField.setSelectionColor(RetroTheme.WHITE);
        fileNameField.setSelectedTextColor(RetroTheme.BLACK);
        fileNameField.setFont(RetroTheme.MONO);
        fileNameField.setBorder(BorderFactory.createLineBorder(RetroTheme.WHITE, 1));
        fileNameField.setFocusTraversalKeysEnabled(false);

        fileNameRow.add(label, BorderLayout.WEST);
        fileNameRow.add(fileNameField, BorderLayout.CENTER);

        statusLabel.setForeground(RetroTheme.WHITE);
        statusLabel.setFont(RetroTheme.MONO_SMALL);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel actionRow = new JPanel();
        actionRow.setBackground(RetroTheme.BLACK);
        actionRow.setLayout(new BoxLayout(actionRow, BoxLayout.X_AXIS));
        actionRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton saveButton = createRetroButton("SAVE", this::submitFileName);
        JButton cancelButton = createRetroButton("CANCEL", cancelAction);
        actionRow.add(saveButton);
        actionRow.add(Box.createHorizontalStrut(12));
        actionRow.add(cancelButton);

        JLabel help = new JLabel("Tab switch area   ↑/↓ select   Enter open/save   Double-click folder   Esc cancel");
        help.setForeground(RetroTheme.DIM);
        help.setFont(RetroTheme.MONO_SMALL);
        help.setAlignmentX(Component.LEFT_ALIGNMENT);

        footer.add(fileNameRow);
        footer.add(Box.createVerticalStrut(8));
        footer.add(statusLabel);
        footer.add(Box.createVerticalStrut(8));
        footer.add(actionRow);
        footer.add(Box.createVerticalStrut(8));
        footer.add(help);
        return footer;
    }

    private JButton createRetroButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.setBackground(RetroTheme.BLACK);
        button.setForeground(RetroTheme.WHITE);
        button.setFont(RetroTheme.MONO);
        button.setBorder(BorderFactory.createLineBorder(RetroTheme.WHITE, 1));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(event -> action.run());
        return button;
    }

    private void installBindings() {
        installCommonBindings(directoryList);
        installCommonBindings(fileNameField);

        InputMap listInput = directoryList.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap listActions = directoryList.getActionMap();
        listInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "openDirectory");
        listActions.put("openDirectory", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                openSelectedEntry();
            }
        });
        listInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "parentDirectory");
        listActions.put("parentDirectory", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                goToParent();
            }
        });

        InputMap fieldInput = fileNameField.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap fieldActions = fileNameField.getActionMap();
        fieldInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "saveFile");
        fieldActions.put("saveFile", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                submitFileName();
            }
        });
    }

    private void installCommonBindings(JComponent component) {
        InputMap input = component.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actions = component.getActionMap();

        input.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "switchArea");
        input.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK), "switchArea");
        actions.put("switchArea", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                switchArea();
            }
        });

        input.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
        actions.put("cancel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                cancelAction.run();
            }
        });
    }

    private void switchArea() {
        if (focusArea == FocusArea.FILE_NAME) {
            focusArea = FocusArea.DIRECTORIES;
            directoryList.requestFocusInWindow();
        } else {
            focusArea = FocusArea.FILE_NAME;
            fileNameField.requestFocusInWindow();
            fileNameField.selectAll();
        }
    }

    private void loadDirectory() {
        model.clear();
        pathLabel.setText(currentDirectory.toString());
        statusLabel.setText(" ");

        Path parent = currentDirectory.getParent();
        if (parent != null) {
            model.addElement(new FileEntry(parent, FileEntry.EntryType.PARENT));
        }

        try (var stream = Files.list(currentDirectory)) {
            List<Path> entries = stream
                    .filter(path -> Files.isDirectory(path)
                            || (Files.isRegularFile(path)
                            && path.getFileName().toString().toLowerCase().endsWith(".txt")))
                    .sorted(Comparator
                            .comparing((Path path) -> !Files.isDirectory(path))
                            .thenComparing(path -> path.getFileName().toString().toLowerCase()))
                    .toList();

            for (Path entry : entries) {
                FileEntry.EntryType type = Files.isDirectory(entry)
                        ? FileEntry.EntryType.DIRECTORY
                        : FileEntry.EntryType.TEXT_FILE;
                model.addElement(new FileEntry(entry, type));
            }

            if (!model.isEmpty()) {
                directoryList.setSelectedIndex(0);
                directoryList.ensureIndexIsVisible(0);
            }
        } catch (IOException | SecurityException exception) {
            statusLabel.setText("Unable to read this folder.");
        }
    }

    private void openSelectedEntry() {
        FileEntry entry = directoryList.getSelectedValue();
        if (entry == null) {
            return;
        }
        if (entry.type() == FileEntry.EntryType.TEXT_FILE) {
            fileNameField.setText(entry.path().getFileName().toString());
            focusArea = FocusArea.FILE_NAME;
            fileNameField.requestFocusInWindow();
            fileNameField.selectAll();
            return;
        }

        currentDirectory = entry.path().toAbsolutePath().normalize();
        loadDirectory();
    }

    private void goToParent() {
        Path parent = currentDirectory.getParent();
        if (parent != null) {
            currentDirectory = parent;
            loadDirectory();
        }
    }

    private void submitFileName() {
        String name = fileNameField.getText().trim();
        if (name.isEmpty()) {
            statusLabel.setText("Enter a filename.");
            return;
        }
        if (name.contains("/") || name.contains("\\")) {
            statusLabel.setText("The filename cannot contain path separators.");
            return;
        }
        saveAction.accept(currentDirectory.resolve(name));
    }

    private Path normalizeInitialDirectory(Path path) {
        if (path != null && Files.isDirectory(path)) {
            return path.toAbsolutePath().normalize();
        }
        return Path.of(System.getProperty("user.home", ".")).toAbsolutePath().normalize();
    }
}
