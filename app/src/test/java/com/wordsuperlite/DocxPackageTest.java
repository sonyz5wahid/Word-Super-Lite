package com.wordsuperlite;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipInputStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DocxPackageTest {
    private static final Charset UTF8 = Charset.forName("UTF-8");

    @Test
    public void roundTripPreservesEveryPartNameAndPayload() throws Exception {
        Map<String, byte[]> sourceParts = sampleParts();
        ByteArrayOutputStream source = makeZip(sourceParts);
        ByteArrayOutputStream destination = new ByteArrayOutputStream();

        int copied = DocxPackage.copyPackage(
                new ByteArrayInputStream(source.toByteArray()), destination, 1024 * 1024);

        assertEquals(sourceParts.size(), copied);
        Map<String, byte[]> resultParts = readZip(destination.toByteArray());
        assertEquals(sourceParts.keySet(), resultParts.keySet());
        for (String name : sourceParts.keySet()) {
            assertArrayEquals("Part differs: " + name, sourceParts.get(name), resultParts.get(name));
        }
    }

    @Test
    public void rejectsArchiveMissingRequiredDocxParts() throws Exception {
        Map<String, byte[]> parts = new LinkedHashMap<String, byte[]>();
        parts.put("[Content_Types].xml", bytes("<Types/>"));
        ByteArrayOutputStream source = makeZip(parts);
        try {
            DocxPackage.copyPackage(new ByteArrayInputStream(source.toByteArray()),
                    new ByteArrayOutputStream(), 1024);
            fail("Expected missing-part validation to fail.");
        } catch (IOException expected) {
            // Expected.
        }
    }

    @Test
    public void rejectsArchiveOverConfiguredUncompressedLimit() throws Exception {
        ByteArrayOutputStream source = makeZip(sampleParts());
        try {
            DocxPackage.copyPackage(new ByteArrayInputStream(source.toByteArray()),
                    new ByteArrayOutputStream(), 16);
            fail("Expected size limit to fail.");
        } catch (IOException expected) {
            // Expected.
        }
    }

    private static Map<String, byte[]> sampleParts() {
        Map<String, byte[]> parts = new LinkedHashMap<String, byte[]>();
        parts.put("[Content_Types].xml", bytes("<Types/>"));
        parts.put("_rels/.rels", bytes("<Relationships/>"));
        parts.put("word/document.xml", bytes("<w:document/>"));
        parts.put("word/header1.xml", bytes("<w:hdr>Header</w:hdr>"));
        parts.put("word/media/image1.png", new byte[] {0, 1, 2, 3, -1});
        parts.put("customXml/item1.xml", bytes("<custom>keep me</custom>"));
        parts.put("custom/unknown.bin", new byte[] {9, 8, 7, 6});
        return parts;
    }

    private static byte[] bytes(String value) {
        return value.getBytes(UTF8);
    }

    private static ByteArrayOutputStream makeZip(Map<String, byte[]> parts) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(output);
        for (Map.Entry<String, byte[]> part : parts.entrySet()) {
            zip.putNextEntry(new ZipEntry(part.getKey()));
            zip.write(part.getValue());
            zip.closeEntry();
        }
        zip.finish();
        return output;
    }

    private static Map<String, byte[]> readZip(byte[] archive) throws IOException {
        Map<String, byte[]> parts = new LinkedHashMap<String, byte[]>();
        ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(archive));
        ZipEntry entry;
        byte[] buffer = new byte[128];
        while ((entry = zip.getNextEntry()) != null) {
            ByteArrayOutputStream data = new ByteArrayOutputStream();
            int count;
            while ((count = zip.read(buffer)) != -1) data.write(buffer, 0, count);
            parts.put(entry.getName(), data.toByteArray());
            zip.closeEntry();
        }
        return parts;
    }
}
