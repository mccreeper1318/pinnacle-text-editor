package org.pinnacle.texteditor.ui;

import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class MainMenuDialogSelfTest {
    private MainMenuDialogSelfTest() {
    }

    public static void run() {
        AtomicBoolean closed = new AtomicBoolean();
        AtomicInteger chosen = new AtomicInteger(-1);
        MainMenuDialog dialog = new MainMenuDialog(
                List.of(
                        new ChoiceDialog.Choice("First", () -> chosen.set(0)),
                        new ChoiceDialog.Choice("Second", () -> chosen.set(1))
                ),
                () -> closed.set(true)
        );

        invoke(dialog, "DOWN");
        invoke(dialog, "ENTER");
        require(chosen.get() == 1, "down and enter did not activate the selected menu item");

        invoke(dialog, "ESCAPE");
        require(closed.get(), "Escape did not close the main menu");
    }

    private static void invoke(MainMenuDialog dialog, String name) {
        Action action = dialog.getActionMap().get(name);
        require(action != null, "missing menu action: " + name);
        action.actionPerformed(new ActionEvent(dialog, ActionEvent.ACTION_PERFORMED, name));
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
