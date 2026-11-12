package com.wordsuperlite;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Build;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.graphics.Typeface;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.*;
import java.io.*;

public class MainActivity extends Activity {
    private EditText editor;
    private TextView status;
    private String currentName = "Dokumen-1.docx";
    private static final int PICK_OPEN = 10, PICK_SAVE = 11;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        buildInterface();
        updateStatus();
    }

    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }

    private void buildInterface() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(238,239,241));

        TextView title = new TextView(this);
        title.setText("Word Super Lite     ▾     " + currentName);
        title.setTextColor(Color.WHITE); title.setTextSize(16);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setPadding(dp(12),0,dp(12),0);
        title.setBackgroundColor(Color.rgb( forty(), 91, 150));
        root.addView(title, new LinearLayout.LayoutParams(-1, dp(42)));

        HorizontalScrollView tabsScroll = new HorizontalScrollView(this);
        tabsScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        String[] names = {"File","Home","Insert","Layout","References","Review","View"};
        for (final String name : names) {
            Button b = tabButton(name);
            b.setOnClickListener(v -> showTab(name));
            tabs.addView(b);
        }
        tabsScroll.addView(tabs);
        root.addView(tabsScroll, new LinearLayout.LayoutParams(-1, dp(42)));

        HorizontalScrollView ribbonScroll = new HorizontalScrollView(this);
        ribbonScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout ribbon = new LinearLayout(this);
        ribbon.setGravity(Gravity.CENTER_VERTICAL);
        ribbon.setPadding(dp(4),dp(3),dp(4),dp(3));
        ribbon.setBackgroundColor(Color.WHITE);
        addAction(ribbon,"B",()->applyStyle(Typeface.BOLD));
        addAction(ribbon,"I",()->applyStyle(Typeface.ITALIC));
        addAction(ribbon,"U",()->Toast.makeText(this,"Underline: tahap berikutnya",Toast.LENGTH_SHORT).show());
        addAction(ribbon,"−",()->changeSize(-1));
        addAction(ribbon,"11",()->changeSize(1));
        addAction(ribbon,"A+",()->changeSize(2));
        addAction(ribbon,"Left",()->editor.setGravity(Gravity.TOP|Gravity.LEFT));
        addAction(ribbon,"Center",()->editor.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL));
        addAction(ribbon,"Right",()->editor.setGravity(Gravity.TOP|Gravity.RIGHT));
        addAction(ribbon,"Justify",()->Toast.makeText(this,"Justify akan ditambahkan pada mesin tata letak",Toast.LENGTH_SHORT).show());
        ribbonScroll.addView(ribbon);
        root.addView(ribbonScroll,new LinearLayout.LayoutParams(-1,dp(54)));

        HorizontalScrollView rulerScroll = new HorizontalScrollView(this);
        TextView ruler = new TextView(this);
        ruler.setText("  0      1      2      3      4      5      6      7      8      9      10      11      12      13      14");
        ruler.setTextColor(Color.DKGRAY); ruler.setTextSize(11);
        ruler.setPadding(dp(12),dp(5),dp(12),dp(5)); ruler.setBackgroundColor(Color.rgb(247,247,247));
        rulerScroll.addView(ruler);
        root.addView(rulerScroll,new LinearLayout.LayoutParams(-1,dp(25)));

        FrameLayout workspace = new FrameLayout(this);
        workspace.setBackgroundColor(Color.rgb(224,226,229));
        ScrollView pageScroll = new ScrollView(this);
        pageScroll.setFillViewport(false);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setBackgroundColor(Color.WHITE);
        page.setPadding(dp(34),dp(42),dp(34),dp(42));
        page.setElevation(dp(2));
        editor = new EditText(this);
        editor.setTextSize(16);
        editor.setTextColor(Color.BLACK);
        editor.setHint("Mulai mengetik dokumen Anda...");
        editor.setGravity(Gravity.TOP|Gravity.LEFT);
        editor.setBackgroundColor(Color.TRANSPARENT);
        editor.setPadding(0,0,0,0);
        editor.setMinLines(28);
        editor.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        page.addView(editor,new LinearLayout.LayoutParams(-1,-2));
        pageScroll.addView(page);
        FrameLayout.LayoutParams pp = new FrameLayout.LayoutParams(-1,-1);
        pp.setMargins(dp(14),dp(12),dp(14),dp(12));
        workspace.addView(pageScroll,pp);
        root.addView(workspace,new LinearLayout.LayoutParams(-1,0,1));

        status = new TextView(this);
        status.setTextColor(Color.DKGRAY); status.setTextSize(11);
        status.setPadding(dp(10),dp(7),dp(10),dp(7));
        status.setBackgroundColor(Color.rgb(248,248,248));
        root.addView(status,new LinearLayout.LayoutParams(-1,dp(30)));
        editor.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            public void onTextChanged(CharSequence s,int st,int before,int count){updateStatus();}
            public void afterTextChanged(android.text.Editable e){}
        });
        setContentView(root);
    }

    private int forty(){ return 47; }
    private Button tabButton(String s) {
        Button b = new Button(this); b.setText(s); b.setTextSize(12);
        b.setTextColor(Color.rgb(35,65,105)); b.setAllCaps(false);
        b.setPadding(dp(10),0,dp(10),0);
        return b;
    }
    private void addAction(LinearLayout row,String label,Runnable action) {
        Button b = new Button(this); b.setText(label); b.setTextSize(12);
        b.setAllCaps(false); b.setMinWidth(dp(42)); b.setPadding(dp(5),0,dp(5),0);
        row.addView(b,new LinearLayout.LayoutParams(-2,dp(44)));
        b.setOnClickListener(v -> action.run());
    }

    private void showTab(String tab) {
        if ("File".equals(tab)) {
            new AlertDialog.Builder(this).setTitle("File")
                .setItems(new String[]{"New","Open DOCX","Save DOCX","Save as DOCX"},(d,w)->{
                    if(w==0) newDocument(); else if(w==1) openDocument();
                    else saveDocument(w==3);
                }).show();
        } else if ("Home".equals(tab)) {
            Toast.makeText(this,"Home: pemformatan dasar tersedia di ribbon.",Toast.LENGTH_SHORT).show();
        } else {
            String msg;
            if ("Insert".equals(tab)) msg="Fitur berikutnya: tabel, gambar, page break.";
            else if ("Layout".equals(tab)) msg="Fitur berikutnya: ukuran kertas, margin, orientasi.";
            else if ("References".equals(tab)) msg="Fitur berikutnya: daftar isi dan catatan kaki.";
            else if ("Review".equals(tab)) msg="Kata: "+wordCount()+". Pemeriksaan ejaan menyusul.";
            else msg="Tampilan halaman dan zoom akan dikembangkan.";
            Toast.makeText(this,msg,Toast.LENGTH_LONG).show();
        }
    }

    private void applyStyle(int style) {
        int a=editor.getSelectionStart(), b=editor.getSelectionEnd();
        if(a==b){ Toast.makeText(this,"Pilih teks terlebih dahulu.",Toast.LENGTH_SHORT).show(); return; }
        android.text.Editable e=editor.getText();
        e.setSpan(new StyleSpan(style),Math.min(a,b),Math.max(a,b),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
    private void changeSize(int delta) {
        float size=editor.getTextSize()/getResources().getDisplayMetrics().scaledDensity;
        editor.setTextSize(Math.max(8,Math.min(36,size+delta)));
    }
    private int wordCount() {
        String s=editor.getText().toString().trim();
        return s.length()==0?0:s.split("\\s+").length;
    }
    private void updateStatus() {
        if(status!=null) status.setText("Halaman 1     "+wordCount()+" kata     Bahasa: Indonesia     100%");
    }
    private void newDocument() {
        editor.setText(""); currentName="Dokumen-baru.docx";
        Toast.makeText(this,"Dokumen baru dibuat.",Toast.LENGTH_SHORT).show();
    }
    private void openDocument() {
        Intent i;
        if (Build.VERSION.SDK_INT >= 19) {
            i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
        } else {
            i=new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
        }
        i.setType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        try { startActivityForResult(Intent.createChooser(i,"Buka dokumen DOCX"),PICK_OPEN); }
        catch(Exception ex){ Toast.makeText(this,"Tidak ada aplikasi pemilih file yang tersedia.",Toast.LENGTH_LONG).show(); }
    }
    private void saveDocument(boolean saveAs) {
        if (Build.VERSION.SDK_INT >= 19) {
            Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE); i.setType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            i.putExtra(Intent.EXTRA_TITLE,currentName);
            try { startActivityForResult(i,PICK_SAVE); }
            catch(Exception ex){ Toast.makeText(this,"Pemilih lokasi penyimpanan tidak tersedia.",Toast.LENGTH_LONG).show(); }
        } else {
            saveLegacy();
        }
    }
    private void saveLegacy() {
        try {
            File dir=new File(Environment.getExternalStorageDirectory(),"WordSuperLite");
            if(!dir.exists() && !dir.mkdirs()) throw new IOException("Folder penyimpanan tidak dapat dibuat.");
            File out=new File(dir,currentName);
            if(!out.getName().toLowerCase().endsWith(".docx")) out=new File(dir,out.getName()+".docx");
            FileOutputStream stream=new FileOutputStream(out);
            DocxStore.writeText(stream,editor.getText().toString());
            Toast.makeText(this,"Disimpan: "+out.getAbsolutePath(),Toast.LENGTH_LONG).show();
        } catch(Exception ex) {
            Toast.makeText(this,"Gagal menyimpan: "+ex.getMessage(),Toast.LENGTH_LONG).show();
        }
    }
    @Override protected void onActivityResult(int req,int res,Intent data) {
        super.onActivityResult(req,res,data);
        if(res!=RESULT_OK || data==null || data.getData()==null) return;
        Uri uri=data.getData();
        try {
            if(req==PICK_OPEN) {
                String text=DocxStore.readText(getContentResolver().openInputStream(uri));
                editor.setText(text);
                currentName=uri.getLastPathSegment()==null?"Dokumen.docx":uri.getLastPathSegment();
                Toast.makeText(this,"Dokumen dibuka. Periksa hasil tata letak sebelum menyunting dokumen kompleks.",Toast.LENGTH_LONG).show();
            } else if(req==PICK_SAVE) {
                DocxStore.writeText(getContentResolver().openOutputStream(uri),editor.getText().toString());
                Toast.makeText(this,"DOCX berhasil disimpan.",Toast.LENGTH_SHORT).show();
            }
        } catch(Exception ex) {
            new AlertDialog.Builder(this).setTitle("Tidak dapat memproses DOCX")
                .setMessage(ex.getMessage()==null?"Terjadi kesalahan saat membaca atau menyimpan file.":ex.getMessage())
                .setPositiveButton("OK",null).show();
        }
    }

    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.isCtrlPressed()) {
            if(keyCode==KeyEvent.KEYCODE_S){saveDocument(false);return true;}
            if(keyCode==KeyEvent.KEYCODE_O){openDocument();return true;}
            if(keyCode==KeyEvent.KEYCODE_N){newDocument();return true;}
            if(keyCode==KeyEvent.KEYCODE_A){editor.selectAll();return true;}
        }
        return super.onKeyDown(keyCode,event);
    }
}
