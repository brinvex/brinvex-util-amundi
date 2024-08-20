package com.brinvex.util.amundi.impl;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;

public class PdfReader {

    List<String> readPdfLines(InputStream pdfInputStream) {

        try (PDDocument document = Loader.loadPDF(new RandomAccessReadBuffer(pdfInputStream))) {
            if (document.isEncrypted()) {
                throw new IllegalArgumentException("Cannot read encrypted pdf");
            }

            PDFTextStripperByArea stripper = new PDFTextStripperByArea();
            stripper.setSortByPosition(true);

            PDFTextStripper tStripper = new PDFTextStripper();

            String text = tStripper.getText(document);

            return Arrays.asList(text.split("\\r?\\n"));

        } catch (InvalidPasswordException e) {
            throw new IllegalArgumentException("Cannot read encrypted pdf", e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
