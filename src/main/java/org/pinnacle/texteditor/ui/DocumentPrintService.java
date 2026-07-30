package org.pinnacle.texteditor.ui;

import javax.print.PrintService;
import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import java.awt.Color;
import java.awt.Font;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;

final class DocumentPrintService {
    static final double PAGE_MARGIN_POINTS = 54.0; // 0.75 inch on each side.

    PrinterJob createPrintJob(String text, Font editorFont, String documentName) {
        PrintService[] availablePrinters = PrinterJob.lookupPrintServices();
        if (availablePrinters.length == 0) {
            throw new IllegalStateException(
                    "No configured printer was found. Add a printer in Linux system settings and try again."
            );
        }

        PrinterJob job = PrinterJob.getPrinterJob();
        if (documentName != null && !documentName.isBlank()) {
            job.setJobName(documentName);
        } else {
            job.setJobName("Pinnacle Text Editor Document");
        }

        PageFormat pageFormat = withSafeMargins(job.defaultPage());
        job.setPrintable(createPrintable(text, editorFont), pageFormat);
        return job;
    }

    static Printable createPrintable(String text, Font editorFont) {
        return createPrintArea(text, editorFont).getPrintable(null, null);
    }

    static JTextArea createPrintArea(String text, Font editorFont) {
        String fontFamily = editorFont == null ? Font.MONOSPACED : editorFont.getFamily();
        JTextArea printArea = new JTextArea(text == null ? "" : text);
        printArea.setFont(new Font(fontFamily, Font.PLAIN, 11));
        printArea.setBackground(Color.WHITE);
        printArea.setForeground(Color.BLACK);
        printArea.setCaretColor(Color.BLACK);
        printArea.setSelectionColor(Color.LIGHT_GRAY);
        printArea.setSelectedTextColor(Color.BLACK);
        printArea.setOpaque(true);
        printArea.setLineWrap(true);
        printArea.setWrapStyleWord(true);
        printArea.setTabSize(4);
        printArea.setBorder(BorderFactory.createEmptyBorder());
        return printArea;
    }

    static PageFormat withSafeMargins(PageFormat original) {
        PageFormat pageFormat = (PageFormat) original.clone();
        Paper paper = (Paper) pageFormat.getPaper().clone();

        double width = paper.getWidth();
        double height = paper.getHeight();
        double horizontalMargin = Math.min(PAGE_MARGIN_POINTS, Math.max(0, (width - 72) / 2));
        double verticalMargin = Math.min(PAGE_MARGIN_POINTS, Math.max(0, (height - 72) / 2));

        paper.setImageableArea(
                horizontalMargin,
                verticalMargin,
                Math.max(72, width - (horizontalMargin * 2)),
                Math.max(72, height - (verticalMargin * 2))
        );
        pageFormat.setPaper(paper);
        return pageFormat;
    }
}
