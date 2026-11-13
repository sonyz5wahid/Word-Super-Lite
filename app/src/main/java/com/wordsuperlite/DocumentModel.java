package com.wordsuperlite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Platform-neutral subset of a Word document.
 *
 * Values use WordprocessingML units: font size in half-points, paragraph
 * indentation/spacing in twips, and page dimensions in twips.
 * The model is intentionally loss-aware: unsupported OOXML is not silently
 * represented as editable content. Opaque package parts must be retained by
 * the package layer when a document is saved.
 */
public final class DocumentModel {
    public final List<Paragraph> paragraphs = new ArrayList<Paragraph>();
    public Section section = new Section();

    public static final class Paragraph {
        public final List<Run> runs = new ArrayList<Run>();
        public String styleId;
        public String alignment = "left";
        public int leftIndentTwips;
        public int rightIndentTwips;
        public int firstLineIndentTwips;
        public int beforeSpacingTwips;
        public int afterSpacingTwips;
        public int lineSpacing;
        public boolean pageBreakBefore;
    }

    public static final class Run {
        public String text = "";
        public boolean bold;
        public boolean italic;
        public boolean underline;
        public boolean strike;
        public int fontSizeHalfPoints = -1;
        public String fontAscii;
        public String colorHex;
        public boolean tab;
        public boolean lineBreak;
        public boolean pageBreak;
    }

    public static final class Section {
        public int pageWidthTwips = 11906;
        public int pageHeightTwips = 16838;
        public int marginTopTwips = 1440;
        public int marginRightTwips = 1440;
        public int marginBottomTwips = 1440;
        public int marginLeftTwips = 1440;
        public String orientation = "portrait";
    }
}
