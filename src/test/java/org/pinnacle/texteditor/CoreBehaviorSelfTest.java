package org.pinnacle.texteditor;

import org.pinnacle.texteditor.ui.DocumentPrintServiceSelfTest;
import org.pinnacle.texteditor.ui.MainMenuDialogSelfTest;
import org.pinnacle.texteditor.update.VersionComparatorSelfTest;

public final class CoreBehaviorSelfTest {
    private CoreBehaviorSelfTest() {
    }

    public static void main(String[] args) throws Exception {
        VersionComparatorSelfTest.run();
        DocumentPrintServiceSelfTest.run();
        MainMenuDialogSelfTest.run();
        System.out.println("Pinnacle Text Editor core self-tests passed.");
    }
}
