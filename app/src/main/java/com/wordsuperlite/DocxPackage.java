package com.wordsuperlite;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Safe, streaming operations for DOCX ZIP packages.
 *
 * This class deliberately copies package parts without interpreting WordprocessingML.
 * It is an isolated foundation, not yet connected to the editor or save workflow.
 */
public final class DocxPackage {
    private static final String CONTENT_TYPES = "[Content_Types].xml";
    private static final String PACKAGE_RELS = "_rels/.rels";
    private static final String DOCUMENT = "word/document.xml";
    private static final int BUFFER_SIZE = 8192;

    private DocxPackage() {
    }

    /**
     * Copies a DOCX package from input to output, preserving every entry name and
     * uncompressed payload byte. The caller retains ownership of both streams.
     *
     * @param input source DOCX ZIP stream
     * @param output destination ZIP stream
     * @param maxUncompressedBytes maximum total uncompressed bytes allowed
     * @return number of package entries copied
     */
    public static int copyPackage(InputStream input, OutputStream output,
                                  long maxUncompressedBytes) throws IOException {
        if (input == null || output == null) {
            throw new IllegalArgumentException("Input and output streams are required.");
        }
        if (maxUncompressedBytes <= 0) {
            throw new IllegalArgumentException("Maximum size must be greater than zero.");
        }

        ZipInputStream zipInput = new ZipInputStream(input);
        ZipOutputStream zipOutput = new ZipOutputStream(output);
        Set<String> names = new HashSet<String>();
        boolean hasContentTypes = false;
        boolean hasPackageRels = false;
        boolean hasDocument = false;
        long totalBytes = 0;
        int entryCount = 0;
        byte[] buffer = new byte[BUFFER_SIZE];

        ZipEntry sourceEntry;
        while ((sourceEntry = zipInput.getNextEntry()) != null) {
            String name = sourceEntry.getName();
            validateEntryName(name);
            if (!names.add(name)) {
                throw new IOException("Duplicate DOCX package entry: " + name);
            }

            if (CONTENT_TYPES.equals(name)) hasContentTypes = true;
            if (PACKAGE_RELS.equals(name)) hasPackageRels = true;
            if (DOCUMENT.equals(name)) hasDocument = true;

            ZipEntry targetEntry = new ZipEntry(name);
            if (sourceEntry.getTime() >= 0) {
                targetEntry.setTime(sourceEntry.getTime());
            }
            zipOutput.putNextEntry(targetEntry);

            int count;
            while ((count = zipInput.read(buffer)) != -1) {
                totalBytes += count;
                if (totalBytes > maxUncompressedBytes) {
                    throw new IOException("DOCX package exceeds the uncompressed size limit.");
                }
                zipOutput.write(buffer, 0, count);
            }
            zipOutput.closeEntry();
            zipInput.closeEntry();
            entryCount++;
        }

        if (!hasContentTypes || !hasPackageRels || !hasDocument) {
            throw new IOException("Invalid DOCX package: required parts are missing.");
        }

        zipOutput.finish();
        zipOutput.flush();
        return entryCount;
    }

    private static void validateEntryName(String name) throws IOException {
        if (name == null || name.length() == 0 || name.charAt(0) == '/'
                || name.indexOf('\\') >= 0) {
            throw new IOException("Unsafe DOCX package entry name.");
        }
        String[] segments = name.split("/");
        for (String segment : segments) {
            if ("..".equals(segment)) {
                throw new IOException("Unsafe DOCX package entry path.");
            }
        }
    }
}
