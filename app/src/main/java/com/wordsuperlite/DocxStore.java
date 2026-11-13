package com.wordsuperlite;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.graphics.Typeface;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * DOCX text bridge for the editor.
 *
 * Current editable subset: paragraphs, text runs, bold, italic, underline,
 * font size, tabs and line breaks. This writer currently creates a basic
 * DOCX package; it does NOT yet round-trip tables, images, headers/footers,
 * comments, footnotes, fields, numbering, section settings or unknown parts.
 * Do not describe this as full-fidelity DOCX support.
 */
public final class DocxStore {
    private DocxStore() { }

    public static CharSequence readStyledText(InputStream input) throws Exception {
        if (input == null) throw new IOException("File tidak dapat dibuka.");
        ZipInputStream zip = new ZipInputStream(new BufferedInputStream(input));
        ZipEntry entry;
        CharSequence result = null;
        try {
            while ((entry = zip.getNextEntry()) != null) {
                if ("word/document.xml".equals(entry.getName())) {
                    ByteArrayOutputStream xml = new ByteArrayOutputStream();
                    byte[] buffer = new byte[8192];
                    int count;
                    while ((count = zip.read(buffer)) != -1) xml.write(buffer, 0, count);
                    result = parseDocument(xml.toByteArray());
                    break;
                }
                zip.closeEntry();
            }
        } finally {
            zip.close();
        }
        if (result == null) throw new IOException("File ini bukan DOCX yang dapat dibaca.");
        return result;
    }

    public static String readText(InputStream input) throws Exception {
        return readStyledText(input).toString();
    }

    private static CharSequence parseDocument(byte[] xmlBytes) throws Exception {
        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        parser.setInput(new java.io.ByteArrayInputStream(xmlBytes), "UTF-8");

        SpannableStringBuilder result = new SpannableStringBuilder();
        int runStart = -1;
        boolean inText = false;
        boolean bold = false, italic = false, underline = false;
        int fontSizeHalfPoints = -1;
        int event;

        while ((event = parser.next()) != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                String name = parser.getName();
                if ("r".equals(name)) {
                    runStart = result.length();
                    bold = false; italic = false; underline = false;
                    fontSizeHalfPoints = -1;
                } else if ("b".equals(name)) {
                    bold = !"0".equals(parser.getAttributeValue(
                            "http://schemas.openxmlformats.org/wordprocessingml/2006/main", "val"));
                } else if ("i".equals(name)) {
                    italic = !"0".equals(parser.getAttributeValue(
                            "http://schemas.openxmlformats.org/wordprocessingml/2006/main", "val"));
                } else if ("u".equals(name)) {
                    String value = parser.getAttributeValue(
                            "http://schemas.openxmlformats.org/wordprocessingml/2006/main", "val");
                    underline = value == null || !"none".equals(value);
                } else if ("sz".equals(name)) {
                    String value = parser.getAttributeValue(
                            "http://schemas.openxmlformats.org/wordprocessingml/2006/main", "val");
                    try { fontSizeHalfPoints = Integer.parseInt(value); }
                    catch (Exception ignored) { fontSizeHalfPoints = -1; }
                } else if ("t".equals(name)) {
                    inText = true;
                } else if ("tab".equals(name)) {
                    result.append('\t');
                } else if ("br".equals(name)) {
                    result.append('\n');
                }
            } else if (event == XmlPullParser.TEXT && inText) {
                result.append(parser.getText());
            } else if (event == XmlPullParser.END_TAG) {
                String name = parser.getName();
                if ("t".equals(name)) inText = false;
                if ("r".equals(name) && runStart >= 0) {
                    int end = result.length();
                    if (end > runStart) {
                        int style = (bold ? Typeface.BOLD : 0) | (italic ? Typeface.ITALIC : 0);
                        if (style != Typeface.NORMAL) {
                            result.setSpan(new StyleSpan(style), runStart, end,
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                        if (underline) result.setSpan(new UnderlineSpan(), runStart, end,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        if (fontSizeHalfPoints > 0) {
                            int dip = Math.max(1, Math.round(fontSizeHalfPoints / 2f));
                            result.setSpan(new AbsoluteSizeSpan(dip, true), runStart, end,
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }
                    runStart = -1;
                } else if ("p".equals(name)) {
                    result.append('\n');
                }
            }
        }
        if (result.length() > 0 && result.charAt(result.length() - 1) == '\n') {
            result.delete(result.length() - 1, result.length());
        }
        return result;
    }

    public static void writeText(OutputStream output, String text) throws Exception {
        writeStyledText(output, text == null ? "" : text);
    }

    public static void writeStyledText(OutputStream output, CharSequence text) throws Exception {
        if (output == null) throw new IOException("Lokasi penyimpanan tidak tersedia.");
        CharSequence content = text == null ? "" : text;
        Spanned styled = content instanceof Spanned ? (Spanned) content : null;

        ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(output));
        put(zip, "[Content_Types].xml",
          "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
          "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">" +
          "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>" +
          "<Default Extension=\"xml\" ContentType=\"application/xml\"/>" +
          "<Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/>" +
          "</Types>");
        put(zip, "_rels/.rels",
          "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
          "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
          "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/>" +
          "</Relationships>");

        StringBuilder body = new StringBuilder();
        int paragraphStart = 0;
        for (int i = 0; i <= content.length(); i++) {
            if (i == content.length() || content.charAt(i) == '\n') {
                appendParagraph(body, content, styled, paragraphStart, i);
                paragraphStart = i + 1;
            }
        }
        String document = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
          "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body>" +
          body +
          "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/><w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" w:header=\"708\" w:footer=\"708\" w:gutter=\"0\"/></w:sectPr>" +
          "</w:body></w:document>";
        put(zip, "word/document.xml", document);
        zip.finish();
        zip.flush();
    }

    private static void appendParagraph(StringBuilder xml, CharSequence text,
                                        Spanned styled, int start, int end) {
        xml.append("<w:p>");
        int position = start;
        while (position < end) {
            boolean bold = hasStyle(styled, position, Typeface.BOLD);
            boolean italic = hasStyle(styled, position, Typeface.ITALIC);
            boolean underline = hasUnderline(styled, position);
            int size = getFontSize(styled, position);
            int next = position + 1;
            while (next < end && bold == hasStyle(styled, next, Typeface.BOLD)
                    && italic == hasStyle(styled, next, Typeface.ITALIC)
                    && underline == hasUnderline(styled, next)
                    && size == getFontSize(styled, next)) next++;

            xml.append("<w:r>");
            if (bold || italic || underline || size > 0) {
                xml.append("<w:rPr>");
                if (bold) xml.append("<w:b/>");
                if (italic) xml.append("<w:i/>");
                if (underline) xml.append("<w:u w:val=\"single\"/>");
                if (size > 0) xml.append("<w:sz w:val=\"").append(size * 2).append("\"/>");
                xml.append("</w:rPr>");
            }
            String value = text.subSequence(position, next).toString();
            if (value.startsWith(" ") || value.endsWith(" ")) {
                xml.append("<w:t xml:space=\"preserve\">");
            } else {
                xml.append("<w:t>");
            }
            xml.append(escape(value)).append("</w:t></w:r>");
            position = next;
        }
        xml.append("</w:p>");
    }

    private static boolean hasStyle(Spanned text, int at, int requested) {
        if (text == null) return false;
        StyleSpan[] spans = text.getSpans(at, at + 1, StyleSpan.class);
        for (StyleSpan span : spans) {
            if ((span.getStyle() & requested) != 0
                    && text.getSpanStart(span) <= at && text.getSpanEnd(span) > at) return true;
        }
        return false;
    }

    private static boolean hasUnderline(Spanned text, int at) {
        if (text == null) return false;
        UnderlineSpan[] spans = text.getSpans(at, at + 1, UnderlineSpan.class);
        for (UnderlineSpan span : spans) {
            if (text.getSpanStart(span) <= at && text.getSpanEnd(span) > at) return true;
        }
        return false;
    }

    private static int getFontSize(Spanned text, int at) {
        if (text == null) return -1;
        AbsoluteSizeSpan[] spans = text.getSpans(at, at + 1, AbsoluteSizeSpan.class);
        for (AbsoluteSizeSpan span : spans) {
            if (text.getSpanStart(span) <= at && text.getSpanEnd(span) > at) {
                return span.getDip() ? span.getSize() : -1;
            }
        }
        return -1;
    }

    private static void put(ZipOutputStream zip, String name, String value) throws Exception {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(value.getBytes("UTF-8"));
        zip.closeEntry();
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
    }
}
