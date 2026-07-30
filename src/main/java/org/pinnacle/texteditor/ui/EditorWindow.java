package org.pinnacle.texteditor.ui;

import org.pinnacle.texteditor.AppInfo;
import org.pinnacle.texteditor.io.TextFileService;
import org.pinnacle.texteditor.update.UpdateInfo;
import org.pinnacle.texteditor.update.UpdateInstaller;
import org.pinnacle.texteditor.update.UpdateService;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultCaret;
import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public final class EditorWindow {
    private enum PendingAction {
        NONE,
        QUIT,
        OPEN,
        NEW,
        UPDATE
    }

    private final JFrame frame = new JFrame("Pinnacle Text Editor");
    private final JTextArea editor = new JTextArea();
    private final OverlayHost overlay = new OverlayHost();
    private final TextFileService fileService = new TextFileService();
    private final UpdateService updateService = new UpdateService();
    private final UpdateInstaller updateInstaller = new UpdateInstaller();
    private final DocumentPrintService printService = new DocumentPrintService();
    private final Path homeDirectory = Path.of(System.getProperty("user.home", "."));

    private Path currentFile;
    private boolean modified;
    private boolean changingDocument;
    private boolean overlayOpen;
    private PendingAction pendingAction = PendingAction.NONE;
    private UpdateInfo pendingUpdate;
    private Path downloadedUpdate;
    private boolean checkingForUpdate;
    private final Path initialFile;

    public EditorWindow(Path initialFile) {
        this.initialFile = initialFile;
        configureFrame();
        configureEditor();
        configureLayout();
        installGlobalKeys();
        installDocumentTracking();
    }

    public void open() {
        frame.setVisible(true);
        enterFullScreen();
        if (initialFile != null) {
            loadInitialFile(initialFile);
        }
        editor.requestFocusInWindow();
        Timer updateTimer = new Timer(1800, event -> checkForUpdates(false));
        updateTimer.setRepeats(false);
        updateTimer.start();
    }

    private void configureFrame() {
        frame.setUndecorated(true);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setBackground(RetroTheme.BLACK);
        frame.getContentPane().setBackground(RetroTheme.BLACK);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                requestQuit();
            }
        });
    }

    private void configureEditor() {
        editor.setBackground(RetroTheme.BLACK);
        editor.setForeground(RetroTheme.WHITE);
        editor.setCaretColor(RetroTheme.WHITE);
        editor.setSelectionColor(RetroTheme.WHITE);
        editor.setSelectedTextColor(RetroTheme.BLACK);
        editor.setFont(RetroTheme.MONO);
        editor.setLineWrap(true);
        editor.setWrapStyleWord(true);
        editor.setTabSize(4);
        editor.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        editor.setFocusTraversalKeysEnabled(false);

        if (editor.getCaret() instanceof DefaultCaret caret) {
            caret.setBlinkRate(500);
        }
    }

    private void configureLayout() {
        JScrollPane scrollPane = new JScrollPane(editor);
        scrollPane.setBorder(null);
        scrollPane.setBackground(RetroTheme.BLACK);
        scrollPane.getViewport().setBackground(RetroTheme.BLACK);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(new FullscreenLayerLayout());
        layeredPane.setBackground(RetroTheme.BLACK);
        layeredPane.add(scrollPane, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(overlay, JLayeredPane.MODAL_LAYER);

        frame.setContentPane(layeredPane);
    }

    private void enterFullScreen() {
        GraphicsDevice device = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();

        if (device.isFullScreenSupported()) {
            device.setFullScreenWindow(frame);
        } else {
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
    }

    private void installGlobalKeys() {
        JComponent root = frame.getRootPane();
        InputMap input = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actions = root.getActionMap();

        bind(input, actions, "main-menu", KeyEvent.VK_ESCAPE, this::showMainMenu);

        input.put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_DOWN_MASK), "quit");
        actions.put("quit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (!overlayOpen) {
                    requestQuit();
                }
            }
        });
    }

    private void bind(InputMap input, ActionMap actions, String name, int keyCode, Runnable action) {
        input.put(KeyStroke.getKeyStroke(keyCode, 0), name);
        actions.put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (!overlayOpen) {
                    action.run();
                }
            }
        });
    }

    private void installDocumentTracking() {
        editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                markModified();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                markModified();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                markModified();
            }
        });
    }

    private void markModified() {
        if (!changingDocument) {
            modified = true;
        }
    }


    private void loadInitialFile(Path file) {
        try {
            String content = fileService.read(file);
            changingDocument = true;
            editor.setText(content);
            editor.setCaretPosition(0);
            changingDocument = false;
            currentFile = file.toAbsolutePath().normalize();
            modified = false;
        } catch (IOException | IllegalArgumentException exception) {
            changingDocument = false;
            Timer messageTimer = new Timer(250, event -> showFileError(
                    "Unable to open",
                    file,
                    exception
            ));
            messageTimer.setRepeats(false);
            messageTimer.start();
        }
    }

    private void checkForUpdates(boolean userInitiated) {
        if (checkingForUpdate) {
            return;
        }
        checkingForUpdate = true;
        if (userInitiated) {
            showOverlay(new BusyDialog("Checking for updates..."));
        }

        updateService.checkForUpdate().whenComplete((result, error) ->
                SwingUtilities.invokeLater(() -> {
                    checkingForUpdate = false;
                    if (userInitiated && overlayOpen) {
                        closeOverlay();
                    }
                    if (error != null) {
                        if (userInitiated) {
                            showMessage("Unable to check for updates.");
                        }
                        return;
                    }
                    if (result.isEmpty()) {
                        if (userInitiated) {
                            showMessage(AppInfo.NAME + " is up to date.");
                        }
                        return;
                    }
                    offerUpdateWhenEditorIsReady(result.get());
                })
        );
    }

    private void offerUpdateWhenEditorIsReady(UpdateInfo update) {
        if (overlayOpen) {
            Timer retry = new Timer(750, event -> offerUpdateWhenEditorIsReady(update));
            retry.setRepeats(false);
            retry.start();
            return;
        }
        pendingUpdate = update;
        showChoice(
                "Version " + update.version() + " is available. Install it now?",
                List.of(
                        new ChoiceDialog.Choice("Yes", () -> {
                            closeOverlay();
                            downloadUpdate(update);
                        }),
                        new ChoiceDialog.Choice("No", () -> {
                            pendingUpdate = null;
                            closeOverlay();
                        })
                ),
                () -> {
                    pendingUpdate = null;
                    closeOverlay();
                }
        );
    }

    private void downloadUpdate(UpdateInfo update) {
        showOverlay(new BusyDialog("Downloading and verifying version " + update.version() + "..."));
        updateService.download(update).whenComplete((path, error) ->
                SwingUtilities.invokeLater(() -> {
                    closeOverlay();
                    if (error != null) {
                        pendingUpdate = null;
                        showMessage("Unable to download or verify the update.");
                        return;
                    }
                    downloadedUpdate = path;
                    if (modified) {
                        pendingAction = PendingAction.UPDATE;
                        promptToSaveChanges("Save changes before installing the update?");
                    } else {
                        installDownloadedUpdate();
                    }
                })
        );
    }

    private void installDownloadedUpdate() {
        pendingAction = PendingAction.NONE;
        if (downloadedUpdate == null) {
            showMessage("The downloaded update could not be found.");
            return;
        }
        if (!updateInstaller.canInstall()) {
            showMessage("Automatic installation requires the installed Linux version.");
            return;
        }
        try {
            updateInstaller.installAndRestart(downloadedUpdate);
            exitApplication();
        } catch (IOException exception) {
            showMessage("Unable to start the update installer.");
        }
    }

    private void showMainMenu() {
        showOverlay(new MainMenuDialog(
                List.of(
                        new ChoiceDialog.Choice("Save Document", () -> runMenuAction(
                                () -> saveDocument(false, this::showSavedConfirmation)
                        )),
                        new ChoiceDialog.Choice("Open Document", () -> runMenuAction(this::requestOpen)),
                        new ChoiceDialog.Choice("New Document", () -> runMenuAction(this::requestNew)),
                        new ChoiceDialog.Choice("Print Document", () -> runMenuAction(this::requestPrint)),
                        new ChoiceDialog.Choice("Check for Updates", () -> runMenuAction(
                                () -> checkForUpdates(true)
                        )),
                        new ChoiceDialog.Choice("Exit Program", () -> runMenuAction(this::requestQuit))
                ),
                this::closeOverlay
        ));
    }

    private void runMenuAction(Runnable action) {
        closeOverlay();
        action.run();
    }

    private void requestPrint() {
        if (editor.getText().isEmpty()) {
            showMessage("There is no text to print.");
            return;
        }

        String documentName = currentFile == null
                ? "Pinnacle Text Editor Document"
                : currentFile.getFileName().toString();
        PrinterJob job;
        try {
            job = printService.createPrintJob(editor.getText(), editor.getFont(), documentName);
        } catch (RuntimeException exception) {
            showMessage("Printing is unavailable: " + exception.getMessage());
            return;
        }

        boolean accepted;
        try {
            accepted = job.printDialog();
        } catch (RuntimeException exception) {
            showMessage("Unable to open the printer dialog: " + exception.getMessage());
            return;
        }
        if (!accepted) {
            editor.requestFocusInWindow();
            return;
        }

        showOverlay(new BusyDialog("Printing document..."));
        CompletableFuture.runAsync(() -> {
            try {
                job.print();
            } catch (PrinterException exception) {
                throw new CompletionException(exception);
            }
        }).whenComplete((ignored, error) -> SwingUtilities.invokeLater(() -> {
            closeOverlay();
            if (error != null) {
                showMessage("Unable to print the document: " + error.getCause().getMessage());
            } else {
                showMessage("Document sent to the printer.");
            }
        }));
    }

    private void requestQuit() {
        showChoice(
                "Are you sure you want to quit?",
                List.of(
                        new ChoiceDialog.Choice("Yes", this::confirmQuit),
                        new ChoiceDialog.Choice("No", this::closeOverlay)
                ),
                this::closeOverlay
        );
    }

    private void confirmQuit() {
        closeOverlay();
        if (modified) {
            pendingAction = PendingAction.QUIT;
            promptToSaveChanges("Save changes before quitting?");
        } else {
            exitApplication();
        }
    }

    private void requestSave() {
        showChoice(
                "Do you want to save the document?",
                List.of(
                        new ChoiceDialog.Choice("Yes", () -> {
                            closeOverlay();
                            saveDocument(false, this::showSavedConfirmation);
                        }),
                        new ChoiceDialog.Choice("No", this::closeOverlay)
                ),
                this::closeOverlay
        );
    }

    private void requestOpen() {
        if (modified) {
            pendingAction = PendingAction.OPEN;
            promptToSaveChanges("Save changes before opening another document?");
        } else {
            showOpenDialog();
        }
    }

    private void requestNew() {
        if (modified) {
            pendingAction = PendingAction.NEW;
            promptToSaveChanges("Save changes before creating a new document?");
        } else {
            createNewDocument();
        }
    }

    private void promptToSaveChanges(String message) {
        showChoice(
                message,
                List.of(
                        new ChoiceDialog.Choice("Yes", () -> {
                            closeOverlay();
                            saveDocument(false, this::continuePendingAction);
                        }),
                        new ChoiceDialog.Choice("No", () -> {
                            closeOverlay();
                            continuePendingAction();
                        }),
                        new ChoiceDialog.Choice("Cancel", () -> {
                            pendingAction = PendingAction.NONE;
                            closeOverlay();
                        })
                ),
                () -> {
                    pendingAction = PendingAction.NONE;
                    closeOverlay();
                }
        );
    }

    private void continuePendingAction() {
        PendingAction action = pendingAction;
        pendingAction = PendingAction.NONE;

        switch (action) {
            case QUIT -> exitApplication();
            case OPEN -> showOpenDialog();
            case NEW -> createNewDocument();
            case UPDATE -> installDownloadedUpdate();
            case NONE -> editor.requestFocusInWindow();
        }
    }

    private void saveDocument(boolean forceSaveAs, Runnable afterSave) {
        if (!forceSaveAs && currentFile != null) {
            writeCurrentDocument(currentFile, afterSave);
            return;
        }
        showSaveDialog(afterSave);
    }

    private void showOpenDialog() {
        Path initial = currentFile != null ? currentFile.getParent() : homeDirectory;
        FileBrowserDialog dialog = new FileBrowserDialog(
                initial,
                fileService,
                this::openDocument,
                this::closeOverlay
        );
        showOverlay(dialog);
    }

    private void openDocument(Path file) {
        try {
            String content = fileService.read(file);
            changingDocument = true;
            editor.setText(content);
            editor.setCaretPosition(0);
            changingDocument = false;
            currentFile = file.toAbsolutePath().normalize();
            modified = false;
            closeOverlay();
        } catch (IOException | IllegalArgumentException exception) {
            changingDocument = false;
            showFileError("Unable to open", file, exception);
        }
    }

    private void showSaveDialog(Runnable afterSave) {
        Path initial = currentFile != null ? currentFile.getParent() : homeDirectory;
        SaveFileDialog dialog = new SaveFileDialog(
                initial,
                path -> prepareSavePath(path, afterSave),
                () -> {
                    pendingAction = PendingAction.NONE;
                    closeOverlay();
                }
        );
        showOverlay(dialog);
    }

    private void prepareSavePath(Path selectedPath, Runnable afterSave) {
        Path finalPath = fileService.ensureTxtExtension(selectedPath).toAbsolutePath().normalize();
        if (Files.exists(finalPath)) {
            showChoice(
                    "That file already exists. Overwrite it?",
                    List.of(
                            new ChoiceDialog.Choice("Yes", () -> {
                                closeOverlay();
                                writeCurrentDocument(finalPath, afterSave);
                            }),
                            new ChoiceDialog.Choice("No", () -> showSaveDialog(afterSave))
                    ),
                    () -> showSaveDialog(afterSave)
            );
        } else {
            closeOverlay();
            writeCurrentDocument(finalPath, afterSave);
        }
    }

    private void writeCurrentDocument(Path path, Runnable afterSave) {
        try {
            Path finalPath = fileService.ensureTxtExtension(path).toAbsolutePath().normalize();
            fileService.write(finalPath, editor.getText());
            currentFile = finalPath;
            modified = false;
            if (afterSave != null) {
                afterSave.run();
            } else {
                editor.requestFocusInWindow();
            }
        } catch (IOException | SecurityException exception) {
            pendingAction = PendingAction.NONE;
            showFileError("Unable to save", path, exception);
        }
    }

    private void showSavedConfirmation() {
        if (currentFile == null) {
            editor.requestFocusInWindow();
            return;
        }
        showMessage("<html><div style='text-align:center'>Document saved successfully.<br><br>"
                + escapeHtml(currentFile.toString())
                + "</div></html>");
    }

    private void showFileError(String action, Path path, Exception exception) {
        exception.printStackTrace(System.err);
        String fileName = path == null ? "No file selected" : path.toAbsolutePath().normalize().toString();
        String reason = exception.getMessage();
        if (reason == null || reason.isBlank()) {
            reason = exception.getClass().getSimpleName();
        }
        showMessage("<html><div style='text-align:center'>"
                + escapeHtml(action)
                + ":<br>"
                + escapeHtml(fileName)
                + "<br><br>Reason: "
                + escapeHtml(reason)
                + "</div></html>");
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private void createNewDocument() {
        changingDocument = true;
        editor.setText("");
        changingDocument = false;
        currentFile = null;
        modified = false;
        closeOverlay();
    }

    private void showChoice(String message, List<ChoiceDialog.Choice> choices, Runnable cancelAction) {
        showOverlay(new ChoiceDialog(message, choices, cancelAction));
    }

    private void showMessage(String message) {
        showOverlay(new MessageDialog(message, this::closeOverlay));
    }

    private void showOverlay(JComponent component) {
        overlayOpen = true;
        editor.setEditable(false);
        overlay.showOverlay(component);
    }

    private void closeOverlay() {
        overlay.hideOverlay();
        overlayOpen = false;
        editor.setEditable(true);
        editor.requestFocusInWindow();
    }

    private void exitApplication() {
        GraphicsDevice device = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();
        if (device.getFullScreenWindow() == frame) {
            device.setFullScreenWindow(null);
        }
        frame.dispose();
        System.exit(0);
    }
}
