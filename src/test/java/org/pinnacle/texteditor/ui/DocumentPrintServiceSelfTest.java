package org.pinnacle.texteditor.ui;

import javax.swing.SwingUtilities;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.util.concurrent.atomic.AtomicReference;

public final class DocumentPrintServiceSelfTest {
    private DocumentPrintServiceSelfTest() {
    }

    public static void run() throws Exception {
        PageFormat original = new PageFormat();
        Paper paper = new Paper();
        paper.setSize(612, 792);
        paper.setImageableArea(0, 0, 612, 792);
        original.setPaper(paper);

        PageFormat formatted = DocumentPrintService.withSafeMargins(original);
        require(close(formatted.getImageableX(), 54), "left print margin is incorrect");
        require(close(formatted.getImageableY(), 54), "top print margin is incorrect");
        require(close(formatted.getImageableWidth(), 504), "printable width is incorrect");
        require(close(formatted.getImageableHeight(), 684), "printable height is incorrect");

        StringBuilder document = new StringBuilder();
        for (int index = 0; index < 300; index++) {
            document.append("This is a deliberately long printable line number ")
                    .append(index)
                    .append(" that should wrap by whole words inside the page margins without being cut off.")
                    .append('\n');
        }

        AtomicReference<Printable> printableReference = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> printableReference.set(DocumentPrintService.createPrintable(
                document.toString(),
                new Font(Font.MONOSPACED, Font.PLAIN, 20)
        )));

        BufferedImage pageImage = new BufferedImage(612, 792, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = pageImage.createGraphics();
        try {
            int pageCount = 0;
            while (pageCount < 50
                    && printableReference.get().print(graphics, formatted, pageCount) == Printable.PAGE_EXISTS) {
                pageCount++;
            }
            require(pageCount > 1, "long documents must paginate onto more than one page");
            require(pageCount < 50, "print pagination did not terminate");
        } finally {
            graphics.dispose();
        }
    }

    private static boolean close(double left, double right) {
        return Math.abs(left - right) < 0.01;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
