package com.wordsuperlite;

import java.io.*;
import java.util.zip.*;
import org.xmlpull.v1.XmlPullParser;
import android.util.Xml;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Minimal DOCX text bridge for the first prototype.
 * Reads and writes basic paragraphs only. It does not preserve advanced
 * formatting, tables, images, headers, footers, sections or footnotes yet.
 */
public final class DocxStore {
    private DocxStore() {}

    public static String readText(InputStream input) throws Exception {
        if(input==null) throw new IOException("File tidak dapat dibuka.");
        ZipInputStream zip=new ZipInputStream(new BufferedInputStream(input));
        ZipEntry entry; String result=null;
        while((entry=zip.getNextEntry())!=null) {
            if("word/document.xml".equals(entry.getName())) {
                ByteArrayOutputStream out=new ByteArrayOutputStream();
                byte[] buf=new byte[4096]; int n;
                while((n=zip.read(buf))>0) out.write(buf,0,n);
                result=extractParagraphs(new ByteArrayInputStream(out.toByteArray()));
                break;
            }
        }
        zip.close();
        if(result==null) throw new IOException("File ini bukan DOCX yang dapat dibaca.");
        return result;
    }

    private static String extractParagraphs(InputStream xml) throws Exception {
        XmlPullParser p=Xml.newPullParser(); p.setInput(xml,"UTF-8");
        StringBuilder all=new StringBuilder(), para=new StringBuilder();
        boolean inText=false, inParagraph=false;
        int event;
        while((event=p.next())!=XmlPullParser.END_DOCUMENT) {
            if(event==XmlPullParser.START_TAG) {
                String name=p.getName();
                if("p".equals(name)){inParagraph=true;para.setLength(0);}
                if("t".equals(name)) inText=true;
                if("tab".equals(name)) para.append('\t');
                if("br".equals(name)) para.append('\n');
            } else if(event==XmlPullParser.TEXT && inText) {
                para.append(p.getText());
            } else if(event==XmlPullParser.END_TAG) {
                String name=p.getName();
                if("t".equals(name)) inText=false;
                if("p".equals(name) && inParagraph) {
                    if(all.length()>0) all.append('\n');
                    all.append(para); inParagraph=false;
                }
            }
        }
        return all.toString();
    }

    public static void writeText(OutputStream output,String text) throws Exception {
        if(output==null) throw new IOException("Lokasi penyimpanan tidak tersedia.");
        ZipOutputStream zip=new ZipOutputStream(new BufferedOutputStream(output));
        put(zip,"[Content_Types].xml",
          "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"+
          "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"+
          "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"+
          "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"+
          "<Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/>"+
          "</Types>");
        put(zip,"_rels/.rels",
          "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"+
          "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"+
          "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/>"+
          "</Relationships>");
        StringBuilder body=new StringBuilder();
        String[] paragraphs=(text==null?"":text).split("\\n",-1);
        for(String line:paragraphs) {
            body.append("<w:p><w:r><w:t xml:space=\"preserve\">")
                .append(escape(line)).append("</w:t></w:r></w:p>");
        }
        String document="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"+
          "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body>"+
          body+"<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/><w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" w:header=\"708\" w:footer=\"708\" w:gutter=\"0\"/></w:sectPr>"+
          "</w:body></w:document>";
        put(zip,"word/document.xml",document);
        zip.finish(); zip.close();
    }
    private static void put(ZipOutputStream zip,String name,String value)throws Exception{
        zip.putNextEntry(new ZipEntry(name));
        zip.write(value.getBytes("UTF-8")); zip.closeEntry();
    }
    private static String escape(String s) {
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")
                .replace("\"","&quot;").replace("'","&apos;");
    }
}
