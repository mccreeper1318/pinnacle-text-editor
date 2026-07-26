package org.pinnacle.texteditor.ui;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.GridBagLayout;

final class OverlayHost extends JPanel {
    OverlayHost() {
        super(new GridBagLayout());
        setOpaque(true);
        setBackground(RetroTheme.BLACK);
        setVisible(false);
        setFocusTraversalKeysEnabled(false);
    }

    void showOverlay(JComponent component) {
        removeAll();
        add(component);
        setVisible(true);
        revalidate();
        repaint();
        component.requestFocusInWindow();
    }

    void hideOverlay() {
        setVisible(false);
        removeAll();
        revalidate();
        repaint();
    }
}
