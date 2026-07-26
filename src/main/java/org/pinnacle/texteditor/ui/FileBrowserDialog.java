package org.pinnacle.texteditor.ui;

import org.pinnacle.texteditor.io.TextFileService;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

final class FileBrowserDialog extends JPanel {
    private final TextFileService fileService;
    private final Consumer<Path> openAction;
    private final Runnable cancelAction;
    private final JLabel pathLabel = new JLabel();
    private final JLabel statusLabel = new JLabel(" ");
    private final DefaultListModel<FileEntry> model = new DefaultListModel<>();
    private final JList<FileEntry> fileList = new JList<>(model);
    private Path currentDirectory;

    FileBrowserDialog(Path initialDirectory,
                      TextFileService fileService,
                      Consumer<Path> openAction,
                      Runnable cancelAction) {
        super(new BorderLayout(0, 12));
        this.fileService = fileService;
        this.openAction = openAction;
        this.cancelAction = cancelAction;
        this.currentDirectory = normalizeInitialDirectory(initialDirectory);

        setBackground(RetroTheme.BLACK);
        setBorder(RetroTheme.dialogBorder());
        setPreferredSize(new Dimension(900, 620));
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        add(createHeader(), BorderLayout.NORTH);
        add(createFileList(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        installKeyBindings();
        loadDirectory();
    }

    @Override
    public boolean requestFocusInWindow() {
        return fileList.requestFocusInWindow();
    }

    private JComponent createHeader() {
        JPanel header = new JPanel();
        header.setBackground(RetroTheme.BLACK);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("OPEN TEXT DOCUMENT");
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

    private JComponent createFileList() {
        fileList.setBackground(RetroTheme.BLACK);
        fileList.setForeground(RetroTheme.WHITE);
        fileList.setSelectionBackground(RetroTheme.SELECTED_BACKGROUND);
        fileList.setSelectionForeground(RetroTheme.SELECTED_FOREGROUND);
        fileList.setFont(RetroTheme.MONO);
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileList.setFixedCellHeight(28);
        fileList.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        fileList.setCellRenderer(new DefaultListCellRenderer() {
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

        JScrollPane scroll = new JScrollPane(fileList);
        scroll.setBorder(BorderFactory.createLineBorder(RetroTheme.WHITE, 1));
        scroll.getViewport().setBackground(RetroTheme.BLACK);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    private JComponent createFooter() {
        JPanel footer = new JPanel();
        footer.setBackground(RetroTheme.BLACK);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));

        statusLabel.setForeground(RetroTheme.WHITE);
        statusLabel.setFont(RetroTheme.MONO_SMALL);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel help = new JLabel("↑/↓ select   Enter open   Backspace parent   Esc cancel");
        help.setForeground(RetroTheme.DIM);
        help.setFont(RetroTheme.MONO_SMALL);
        help.setAlignmentX(Component.LEFT_ALIGNMENT);

        footer.add(statusLabel);
        footer.add(Box.createVerticalStrut(5));
        footer.add(help);
        return footer;
    }

    private void installKeyBindings() {
        InputMap input = fileList.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actions = fileList.getActionMap();

        input.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "openSelected");
        actions.put("openSelected", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                openSelected();
            }
        });

        input.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "parentDirectory");
        actions.put("parentDirectory", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                goToParent();
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

    private void loadDirectory() {
        model.clear();
        pathLabel.setText(currentDirectory.toString());
        statusLabel.setText(" ");

        Path parent = currentDirectory.getParent();
        if (parent != null) {
            model.addElement(new FileEntry(parent, FileEntry.EntryType.PARENT));
        }

        try (var stream = Files.list(currentDirectory)) {
            List<Path> paths = stream
                    .filter(path -> Files.isDirectory(path) || fileService.isTextFile(path))
                    .sorted(Comparator
                            .comparing((Path path) -> !Files.isDirectory(path))
                            .thenComparing(path -> path.getFileName().toString().toLowerCase()))
                    .toList();

            for (Path path : paths) {
                FileEntry.EntryType type = Files.isDirectory(path)
                        ? FileEntry.EntryType.DIRECTORY
                        : FileEntry.EntryType.TEXT_FILE;
                model.addElement(new FileEntry(path, type));
            }

            if (!model.isEmpty()) {
                fileList.setSelectedIndex(0);
                fileList.ensureIndexIsVisible(0);
            } else {
                statusLabel.setText("No folders or .txt files found.");
            }
        } catch (IOException | SecurityException exception) {
            statusLabel.setText("Unable to read this folder.");
        }
    }

    private void openSelected() {
        FileEntry entry = fileList.getSelectedValue();
        if (entry == null) {
            return;
        }

        if (entry.type() == FileEntry.EntryType.TEXT_FILE) {
            openAction.accept(entry.path());
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

    private Path normalizeInitialDirectory(Path path) {
        if (path != null && Files.isDirectory(path)) {
            return path.toAbsolutePath().normalize();
        }
        return Path.of(System.getProperty("user.home", ".")).toAbsolutePath().normalize();
    }
}
