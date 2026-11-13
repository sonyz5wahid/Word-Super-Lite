package com.wordsuperlite;

import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import java.io.InputStream;

/**
 * Reads a deliberately documented editable subset of WordprocessingML.
 * It does not claim to preserve unsupported elements; package-level
 * preservation remains the responsibility of DocxPackage.
 */
public final class OoxmlReader {
    private static final String W =
        "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
    private OoxmlReader() { }

    public static DocumentModel readDocumentXml(InputStream input) throws Exception {
        if (input == null) throw new IllegalArgumentException("Input XML wajib diisi.");
        XmlPullParser p = Xml.newPullParser();
        p.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        p.setInput(input, "UTF-8");

        DocumentModel doc = new DocumentModel();
        DocumentModel.Paragraph para = null;
        DocumentModel.Run run = null;
        StringBuilder text = null;
        int event;
        while ((event = p.next()) != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                String n = p.getName();
                if ("p".equals(n)) {
                    para = new DocumentModel.Paragraph();
                } else if ("pStyle".equals(n) && para != null) {
                    para.styleId = attr(p, "val");
                } else if ("jc".equals(n) && para != null) {
                    String v = attr(p, "val");
                    if (v != null) para.alignment = v;
                } else if ("ind".equals(n) && para != null) {
                    para.leftIndentTwips = integer(attr(p, "left"), 0);
                    para.rightIndentTwips = integer(attr(p, "right"), 0);
                    para.firstLineIndentTwips = integer(attr(p, "firstLine"), 0)
                        - integer(attr(p, "hanging"), 0);
                } else if ("spacing".equals(n) && para != null) {
                    para.beforeSpacingTwips = integer(attr(p, "before"), 0);
                    para.afterSpacingTwips = integer(attr(p, "after"), 0);
                    para.lineSpacing = integer(attr(p, "line"), 0);
                } else if ("pageBreakBefore".equals(n) && para != null) {
                    para.pageBreakBefore = !"0".equals(attr(p, "val"));
                } else if ("r".equals(n) && para != null) {
                    run = new DocumentModel.Run();
                    text = new StringBuilder();
                } else if ("b".equals(n) && run != null) {
                    run.bold = !"0".equals(attr(p, "val"));
                } else if ("i".equals(n) && run != null) {
                    run.italic = !"0".equals(attr(p, "val"));
                } else if ("u".equals(n) && run != null) {
                    run.underline = !"none".equals(attr(p, "val"));
                } else if ("strike".equals(n) && run != null) {
                    run.strike = !"0".equals(attr(p, "val"));
                } else if ("sz".equals(n) && run != null) {
                    run.fontSizeHalfPoints = integer(attr(p, "val"), -1);
                } else if ("rFonts".equals(n) && run != null) {
                    run.fontAscii = attr(p, "ascii");
                } else if ("color".equals(n) && run != null) {
                    run.colorHex = attr(p, "val");
                } else if ("t".equals(n) && run != null) {
                    // Character content is collected by the TEXT event below.
                } else if ("tab".equals(n) && run != null) {
                    run.tab = true;
                    text.append('\t');
                } else if ("br".equals(n) && run != null) {
                    String type = attr(p, "type");
                    if ("page".equals(type)) run.pageBreak = true;
                    else run.lineBreak = true;
                    text.append('\n');
                } else if ("pgSz".equals(n)) {
                    doc.section.pageWidthTwips = integer(attr(p, "w"), doc.section.pageWidthTwips);
                    doc.section.pageHeightTwips = integer(attr(p, "h"), doc.section.pageHeightTwips);
                    if (doc.section.pageWidthTwips > doc.section.pageHeightTwips)
                        doc.section.orientation = "landscape";
                } else if ("pgMar".equals(n)) {
                    doc.section.marginTopTwips = integer(attr(p, "top"), doc.section.marginTopTwips);
                    doc.section.marginRightTwips = integer(attr(p, "right"), doc.section.marginRightTwips);
                    doc.section.marginBottomTwips = integer(attr(p, "bottom"), doc.section.marginBottomTwips);
                    doc.section.marginLeftTwips = integer(attr(p, "left"), doc.section.marginLeftTwips);
                }
            } else if (event == XmlPullParser.TEXT && run != null) {
                text.append(p.getText());
            } else if (event == XmlPullParser.END_TAG) {
                String n = p.getName();
                if ("r".equals(n) && run != null && para != null) {
                    run.text = text.toString();
                    para.runs.add(run);
                    run = null;
                    text = null;
                } else if ("p".equals(n) && para != null) {
                    doc.paragraphs.add(para);
                    para = null;
                }
            }
        }
        return doc;
    }

    private static String attr(XmlPullParser p, String localName) {
        return p.getAttributeValue(W, localName);
    }

    private static int integer(String value, int fallback) {
        if (value == null) return fallback;
        try { return Integer.parseInt(value); }
        catch (NumberFormatException ignored) { return fallback; }
    }
}
